package pl.degree.alertly.infrastructure.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.degree.alertly.infrastructure.model.UserInfoEntity;

import java.util.List;

public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    @Query("SELECT u.username FROM UserInfoEntity u")
    List<String> findAllUsernames();

    @Query(value = """
    SELECT *
    FROM user_info_entity u
    WHERE :token = ANY(u.friends_un)
    """, nativeQuery = true)
    List<UserInfoEntity> findUsersWhoHaveMeAsFriend(String token);
}
