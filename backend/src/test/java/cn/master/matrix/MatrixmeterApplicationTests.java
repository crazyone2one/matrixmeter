package cn.master.matrix;

import cn.master.matrix.entity.User;
import cn.master.matrix.mapper.UserMapper;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@SpringBootTest
class MatrixmeterApplicationTests {
	@Resource
	PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
		LocalDate now = LocalDate.now();
		int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
		System.out.println(currentQuarter);
	}

}
