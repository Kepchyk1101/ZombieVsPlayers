package dev.kepchyk1101.zvp.exception;

public class GameAlreadyStarted extends RuntimeException {
  
  public GameAlreadyStarted(String message) {
    super(message);
  }
  
}
