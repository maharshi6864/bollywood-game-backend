package com.maharshi.bollywood_game_spring_boot.repository;


import com.maharshi.bollywood_game_spring_boot.model.OTPVo;
import com.maharshi.bollywood_game_spring_boot.model.UserVo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OTPRepository extends CrudRepository<OTPVo,Integer> {

    List<OTPVo> findByUserVo(UserVo userVo);
}
