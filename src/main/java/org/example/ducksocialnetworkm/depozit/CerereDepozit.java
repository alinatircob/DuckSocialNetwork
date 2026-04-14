package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.relatie.CererePrietenie;

import java.util.List;

public interface CerereDepozit extends Depozit<CererePrietenie>{
    List<CererePrietenie> findPendingRequests(Long idUser);
    public boolean existaCerereActiva(Long idExp, Long idDest);
    public CererePrietenie findPendingBetween(Long idExp, Long idDest);
}
