package insper.classroom.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackAccountCreate")
    public Account create(Account in) {
        in.hash(calculateHash(in.password()));
        in.password(null);
        return accountRepository.save(new AccountModel(in)).to();
    }

    public Account fallbackAccountCreate(Account in, Throwable t) {
        throw new RuntimeException("Failed to create account", t);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackAccountRead")
    public Account read(@NonNull String id) {
        return accountRepository.findById(id).map(AccountModel::to).orElse(null);
    }

    public Account fallbackAccountRead(String id, Throwable t) {
        throw new RuntimeException("Failed to read account", t);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackAccountReadByEmail")
    public Account login(String email, String password) {
        String hash = calculateHash(password);
        return accountRepository.findByEmailAndHash(email, hash).map(AccountModel::to).orElse(null);
    }

    public Account fallbackAccountReadByEmail(String email, String password, Throwable t) {
        throw new RuntimeException("Failed to read account by email", t);
    }

    private String calculateHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            byte[] encoded = Base64.getEncoder().encode(hash);
            return new String(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
}
