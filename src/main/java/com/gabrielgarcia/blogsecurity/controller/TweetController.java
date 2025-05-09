package com.gabrielgarcia.blogsecurity.controller;

import com.gabrielgarcia.blogsecurity.entities.Role;
import com.gabrielgarcia.blogsecurity.entities.Tweet;
import com.gabrielgarcia.blogsecurity.entities.dto.CreateTweet;
import com.gabrielgarcia.blogsecurity.entities.dto.FeedDto;
import com.gabrielgarcia.blogsecurity.entities.dto.FeedItemDto;
import com.gabrielgarcia.blogsecurity.repositories.TweetRepository;
import com.gabrielgarcia.blogsecurity.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
public class TweetController {

    private TweetRepository tweetRepository;
    private UserRepository userRepository;

    public TweetController(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        var tweets = tweetRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(tweet -> new FeedItemDto(tweet.getId(), tweet.getContent(), tweet.getUser().getUsername()));


        return ResponseEntity.ok(new FeedDto(tweets.getContent(),page, pageSize, tweets.getTotalPages(), tweets.getTotalElements()));
    }

    @PostMapping("/tweets")
    public ResponseEntity<Void> addTweet(@RequestBody CreateTweet createTweet, JwtAuthenticationToken token){
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var tweet = new Tweet();
        tweet.setUser(user.get());
        tweet.setContent(createTweet.content());
        tweetRepository.save(tweet);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId, JwtAuthenticationToken token){
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var tweet = tweetRepository.findById(tweetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || tweet.getUser().getId().equals(UUID.fromString(token.getName()))){
            tweetRepository.deleteById(tweetId);
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }
}
