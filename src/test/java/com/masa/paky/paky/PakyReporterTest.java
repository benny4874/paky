package com.masa.paky.paky;

import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.paky.exceptions.PakyNotPluggedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.stream.Stream;

import static com.masa.paky.paky.entity.PakyStatus.OPERATING;
import static com.masa.paky.paky.entity.PakyStatus.SOLD;
import static com.masa.paky.paky.entity.TraciabilityStatus.ERROR;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PakyReporterTest {

    public static final int A_QUANTITY = 100;
    @Mock
    PakyRepository repository;


    @InjectMocks
    PakyReporter underTest;

    public PakyReporterTest() {
        openMocks(this);
    }

    public static Stream<Arguments> getQuantity() {
        return Stream.of(
                Arguments.of(25, 25, .25f),
                Arguments.of(100, 100, 1),
                Arguments.of(75, 75, .75f),
                Arguments.of(0, 0, 0)
        );
    }

    @Test
    void report_pakyWith100_now50_set50pct_50quantiry_sameOriginal() {
        Paky paky = new Paky();
        paky.setQuantity(100);
        paky.setOriginalQuantity(100);
        paky.setQuantityPct(1);
        paky.setStep(OPERATING);
        paky.setIdPaky("IamPaky");
        when(repository.findById("IamPaky")).thenReturn(of(paky));
        underTest.report("IamPaky", 50f);
        assertAll(
                () -> assertEquals(50f, paky.getQuantity()),
                () -> assertEquals(.5, paky.getQuantityPct()),
                () -> assertEquals(100, paky.getOriginalQuantity())
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"PakyThatNotExists"})
    void report_pakyNotExists_throwPAkyNotFoundException(String pakyId) {
        assertThatExceptionOfType(PakyNotFoundException.class)
                .isThrownBy(() -> underTest.report(pakyId, 50f));

    }

    @ParameterizedTest(name = "A paky report {0} should remain {1} wich is {2} pct")
    @MethodSource("getQuantity")
    void report_pakyWith100_decrease(float quantity, float expectedQuantity, float expectedPercentage) {
        Paky paky = new Paky();
        paky.setQuantity(100);
        paky.setOriginalQuantity(100);
        paky.setQuantityPct(1);
        paky.setStep(OPERATING);
        paky.setIdPaky("IamPaky");
        when(repository.findById("IamPaky")).thenReturn(of(paky));
        underTest.report("IamPaky", quantity);
        assertAll(
                () -> assertEquals(expectedQuantity, paky.getQuantity()),
                () -> assertEquals(expectedPercentage, paky.getQuantityPct())

        );
    }

    @ParameterizedTest(name = "{0} is not a valid quantity, paky is updated but get error 900")
    @ValueSource(floats = {-1f, 101f})
    void reportInvalidQuantity_save_butError(float quantity) {
        Paky paky = new Paky();
        paky.setQuantity(100);
        paky.setOriginalQuantity(100);
        paky.setQuantityPct(1);
        paky.setStep(OPERATING);
        paky.setIdPaky("IamPaky");
        when(repository.findById("IamPaky")).thenReturn(of(paky));

        underTest.report("IamPaky", quantity);

        assertAll(
                () -> assertEquals(quantity, paky.getQuantity()),
                () -> assertEquals(ERROR, paky.getTraciabilityStatus()),
                () -> assertEquals(900, paky.getErrorCode())
        );
        verify(repository).update(paky);
    }

    @ParameterizedTest(name = "{0} is not a valid quantity, paky percentage is set to {1}")
    @CsvSource({
            "-1,0",
            "101,1",
            "101.2,1"
    })
    void reportInvalidQuantity_save_pctIsDefalut(float quantity,float expectedQuantityPct) {
        Paky paky = new Paky();
        paky.setQuantity(100);
        paky.setOriginalQuantity(100);
        paky.setQuantityPct(1);
        paky.setStep(OPERATING);
        paky.setIdPaky("IamPaky");
        when(repository.findById("IamPaky")).thenReturn(of(paky));

        underTest.report("IamPaky", quantity);

       assertEquals(expectedQuantityPct,paky.getQuantityPct());
        verify(repository).update(paky);
    }



    @Test
    void report_pakyNotOperating_SaveQauntity_error1000() {
        Paky paky = new Paky();
        paky.setQuantity(100);
        paky.setOriginalQuantity(100);
        paky.setQuantityPct(1);
        paky.setStep(SOLD);
        paky.setIdPaky("IamPaky");
        when(repository.findById("IamPaky")).thenReturn(of(paky));
        try {
            underTest.report("IamPaky", A_QUANTITY);
        } catch (IllegalStateException e) {
            //do nothing
        }
        assertAll(
                () -> assertEquals(ERROR, paky.getTraciabilityStatus()),
                () -> assertEquals(1000, paky.getErrorCode())
        );
        verify(repository).update(paky);
    }

    @Test
    void report_pakyNotOperating_throwsPakyNotPluggedException() {
        Paky paky = new Paky();
        paky.setQuantity(100);
        paky.setOriginalQuantity(100);
        paky.setQuantityPct(1);
        paky.setStep(SOLD);
        paky.setIdPaky("IamPaky");
        when(repository.findById("IamPaky")).thenReturn(of(paky));
        assertThatExceptionOfType(PakyNotPluggedException.class)
                .isThrownBy(() -> underTest.report("IamPaky", A_QUANTITY));


    }
}
