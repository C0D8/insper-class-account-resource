package insper.classroom.account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<AccountModel, String>{

    public Optional<AccountModel> findByEmailAndHash(String email, String hash);

}
