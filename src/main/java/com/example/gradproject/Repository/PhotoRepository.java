package com.example.gradproject.Repository;

import com.example.gradproject.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Image, Long> {

}
