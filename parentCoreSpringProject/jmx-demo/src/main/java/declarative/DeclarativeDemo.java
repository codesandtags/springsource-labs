package declarative;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DeclarativeDemo {

	public static void main(String[] args) throws IOException {
		new ClassPathXmlApplicationContext("declarative/declarative-config.xml");
		System.out.print("Ready.  Press Enter to Stop...");
		System.in.read();
	}

}
