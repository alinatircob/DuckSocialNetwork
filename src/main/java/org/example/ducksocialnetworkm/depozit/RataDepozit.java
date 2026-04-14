package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.depozit.paging.Page;
import org.example.ducksocialnetworkm.depozit.paging.Pageable;
import org.example.ducksocialnetworkm.depozit.paging.PagingRepository;
import org.example.ducksocialnetworkm.depozit.paging.UserRepository;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.TipRata;
import org.example.ducksocialnetworkm.domeniu.user.User;

import java.util.List;

public interface RataDepozit extends UserRepository{

    public List<Rata> getRate(TipRata tip);

    Page<Rata> findAllRateByTip(Pageable pageable, TipRata tip);
}