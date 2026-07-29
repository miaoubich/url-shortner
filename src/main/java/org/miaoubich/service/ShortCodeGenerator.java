package org.miaoubich.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DEFAULT_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        var sb = new StringBuilder(DEFAULT_LENGTH);
        random.ints(DEFAULT_LENGTH, 0, ALPHABET.length())// creates 7 random integers between 0 and ALPHABET.lenght()-1              
        .forEach(i -> sb.append(ALPHABET.charAt(i)));// picks each chars based on the integers as indexes and appends them
        return sb.toString();
    }

}