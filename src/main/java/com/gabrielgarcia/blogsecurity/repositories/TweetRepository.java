package com.gabrielgarcia.blogsecurity.repositories;

import com.gabrielgarcia.blogsecurity.entities.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

}
