package cn.master.matrix;

import cn.master.matrix.entity.User;
import cn.master.matrix.mapper.UserMapper;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class MatrixmeterApplicationTests {
	@Resource
	PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
		System.out.println(UpdateChain.of(User.class)
						.set(User::getPassword, passwordEncoder.encode("admin"))
						.where(User::getId).eq("admin")
				.update());
	}

}
