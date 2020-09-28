import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
 
/**
 * Criptografa e descriptografa uma String
 * @author Lucas
 */
public class AES {

    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            /* Serializa chave */
            key = myKey.getBytes("UTF-8");
            /* Hash na chave */
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            /* Preenchendo espaco se necessário */
            key = Arrays.copyOf(key, 16); 
            /* Gerando chave secreta */
            secretKey = new SecretKeySpec(key, "AES");
        } 
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            /* Seta a chave */
            setKey(secret);
            /* Instancia um objeto Cipher */
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            /* Criptografa a entrada e retorna a string criptografada */
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) {
            System.out.println("Erro ao criptografar: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            /* Seta a chave */
            setKey(secret);
            /* Instancia o objeto Cipher */
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            /* Descriptografa a entrada e retorna a string descriptografada */
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) {
            System.out.println("Erro ao descriptografar: " + e.toString());
        }
        return null;
    }
}
