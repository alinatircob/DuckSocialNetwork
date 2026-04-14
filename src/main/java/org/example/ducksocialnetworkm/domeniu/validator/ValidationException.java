package org.example.ducksocialnetworkm.domeniu.validator;

/**
 * Excepție specifică domeniului de validare.
 * Este aruncată atunci când un obiect nu respectă regulile definite în validator.
 */

public class ValidationException extends RuntimeException {

    /**
     * Creează o excepție de validare cu un mesaj personalizat.
     * @param message mesajul de eroare
     */

    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creează o excepție de validare cu un mesaj și o cauză.
     * @param message mesajul de eroare
     * @param cause cauza excepției
     */

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creează o excepție de validare cu o cauză specificată.
     * @param cause cauza excepției
     */

    public ValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creează o excepție de validare cu opțiuni avansate de configurare.
     * @param message mesajul de eroare
     * @param cause cauza excepției
     * @param enableSuppression permite suprimarea excepțiilor
     * @param writableStackTrace permite scrierea stack trace-ului
     */

    public ValidationException(String message, Throwable cause,
                               boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
