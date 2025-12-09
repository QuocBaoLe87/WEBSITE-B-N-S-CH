import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123";
        String hash = "$2a$10$SQhmpOo/ORTOtWTIU3zvTeP7PBP/Oitw6OWtW0ohWpELEEndAPfbO";
        
        boolean matches = encoder.matches(password, hash);
        System.out.println("Password '123' matches hash: " + matches);
        
        // Tạo hash mới của '123' để so sánh
        String newHash = encoder.encode(password);
        System.out.println("New hash of '123': " + newHash);
    }
}
