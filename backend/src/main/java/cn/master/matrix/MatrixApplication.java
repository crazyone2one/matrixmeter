package cn.master.matrix;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 11‘s papa
 */
@SpringBootApplication
@MapperScan("cn.master.matrix.mapper")
public class MatrixApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatrixApplication.class, args);
	}

}
