package com.example.wallet_api.exception;

public class DuplicateResourceException extends RuntimeException{
    public  DuplicateResourceException(String msg){
        super(msg);
    }
}