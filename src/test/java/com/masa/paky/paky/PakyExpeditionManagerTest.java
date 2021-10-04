package com.masa.paky.paky;


import com.masa.paky.Addressable;
import com.masa.paky.AddressableFinder;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.entity.PakyStatus;
import com.masa.paky.paky.exceptions.DestinationMissMatchException;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.paky.exceptions.PakyNotInTransitException;
import com.masa.paky.vendor.exceptions.VendorNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.Optional;

import static com.masa.paky.paky.entity.PakyStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PakyExpeditionManagerTest {
    @Mock
    AddressableFinder<Addressable, Serializable> repository;
    @Mock
    PakyRepository pakyRepository;

    @Mock
    Addressable recipient;

    PakyExpeditionManager<Addressable, Serializable> underTest;

    public PakyExpeditionManagerTest() {
        MockitoAnnotations.openMocks(this);
        underTest = new PakyExpeditionManager<>(repository,pakyRepository);
        when(repository.findById("aVendor")).thenReturn(Optional.of(recipient));
        when(repository.findById(not(eq("aVendor")))).thenReturn(Optional.empty());
    }

    @Test
    void paky_sentToCustomer_StepIntransit() {
        Paky fixture = agivenPakyToSend();
        underTest.send(fixture.getIdPaky(),"aVendor");
        assertEquals(INTRANSIT,fixture.getStep());
        verify(pakyRepository).update(fixture);
    }

    @Test
    void paky_sentToWrongCustomer_ThrowDestinationMissMatchException() {
        Paky fixture = agivenPakyToSend();
        fixture.setVendorId("anotherVendor");
        assertThrows(DestinationMissMatchException.class , () ->underTest.send(fixture.getIdPaky(),"aVendor"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"aVendorThatNotExists"})
    void paky_sentNotExistingVendor_ThrowVendorNotFoundException(String notExistingVendorId) {
        Paky fixture = agivenPakyToSend();
        fixture.setVendorId(notExistingVendorId);
        assertThrows(VendorNotFoundException.class , () ->underTest.send(fixture.getIdPaky(),"aVendorThatNotExists"));
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"pakyThatNotExists"})
    void send_pakyNotExists_throwPakyNotFounException(String notExistingPAkyId){
        assertThrows(PakyNotFoundException.class , () ->underTest.send(notExistingPAkyId,"aVendor"));
    }




    @ParameterizedTest(name = "{0} sent to {2}")
    @CsvSource({
            "aPaky,aVendorThatNotExists,aVendorThatNotExists",
            "aPaky,aVendor,anotherVendor",
            "aPakyThatNotExists,aVendor,aVendor"
    })
    void expeditionFail_StepNotChange_pakyNotSaved(String pakyId,String expectedVendor,String realVendor){
        Paky fixture = agivenPakyToSend();
        fixture.setVendorId(expectedVendor);
        PakyStatus originalStatus = fixture.getStep();
        send(pakyId, realVendor);
        verifyNoActionIsTaken(fixture, originalStatus);
    }

    @Test
    void paky_receiveCustomer_StepReceived() {
        Paky fixture = agivenPakySent();
        underTest.receive(fixture.getIdPaky(),"aVendor");
        assertEquals(DELIVERED,fixture.getStep());
        verify(pakyRepository).update(fixture);
    }

    @Test
    void paky_receivedbyWrongCustomer_ThrowDestinationMissMatchException() {
        Paky fixture = agivenPakySent();
        fixture.setVendorId("anotherVendor");
        assertThrows(DestinationMissMatchException.class ,
                () ->underTest.receive(fixture.getIdPaky(),"aVendor"));
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"aVendorThatNotExists"})
    void paky_receivedNotExistingCustomer_ThrowVendorNotFoundException(String notExistingVendorId) {
        Paky fixture = agivenPakySent();
        fixture.setVendorId(notExistingVendorId);
        assertThrows(VendorNotFoundException.class ,
                () ->underTest.receive(fixture.getIdPaky(),"aVendorThatNotExists"));
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"pakyThatNotExists"})
    void receive_pakyNotExists_throwPakyNotFounException(String notExistingPAkyId){
        assertThrows(PakyNotFoundException.class , () ->underTest.receive(notExistingPAkyId,"aVendor"));
    }




    @ParameterizedTest(name = "{0} sent to {2}")
    @CsvSource({
            "aPaky,aVendorThatNotExists,aVendorThatNotExists",
            "aPaky,aVendor,anotherVendor",
            "aPakyThatNotExists,aVendor,aVendor"
    })
    void receiveFail_StepNotChange_pakyNotSaved(String pakyId,String expectedVendor,String realVendor){
        Paky fixture = agivenPakySent();
        fixture.setVendorId(expectedVendor);
        PakyStatus originalStatus = fixture.getStep();
        receive(pakyId, realVendor);
        verifyNoActionIsTaken(fixture, originalStatus);
    }

    @Test
    void receive_pakyNotInTransit_pakyNotInTransitException_noActionIsTaken(){
        Paky fixture = agivenPakyToSend();
        fixture.setStep(CREATED);
        PakyStatus originalStatus = fixture.getStep();
        assertThrows(PakyNotInTransitException.class,
                ()-> underTest.receive(
                        fixture.getIdPaky(),
                        fixture.getVendorId())
        );
        verifyNoActionIsTaken(fixture, originalStatus);
    }

    private void verifyNoActionIsTaken(Paky fixture, PakyStatus originalStatus) {
        verify(pakyRepository, never()).update(fixture);
        assertEquals(originalStatus, fixture.getStep());
    }

    private void receive(String pakyId, String realVendor) {
        try{
            underTest.receive(pakyId, realVendor);
        } catch (Exception e){
            // do Nothing
        }
    }


    private Paky agivenPakySent() {
        final Paky paky = agivenPakyToSend();
        paky.setStep(INTRANSIT);
        return paky;
    }

    private void send(String pakyId, String realVendor) {
        try{
            underTest.send(pakyId, realVendor);
        } catch (Exception e){
            // do Nothing
        }
    }

    private Paky agivenPakyToSend() {
        Paky fixture = new Paky();
        fixture.setIdPaky("aPaky");
        fixture.setVendorId("aVendor");
        fixture.setStep(ASSIGNED);
        makeSearchable(fixture);
        return fixture;
    }

    private void makeSearchable(Paky fixture) {
        when(pakyRepository.findById("aPaky")).thenReturn(Optional.of(fixture));
    }


}
