package server.utils;

import org.apache.commons.codec.binary.Base64;
import server.CustomException;
import server.Server;
import server.routes.UsersRoute;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class EncryptUtils {
    private static String userKey = Server.prop.getProperty("userKey", "YMcMoEGD#t6B4vAP");
    private static String userVector = Server.prop.getProperty("userVector", "sXdN&b@QIgzOdrVk");
    private static String masterKey = Server.prop.getProperty("masterKey", "!ze*nFGT0N$PDAnY");
    private static String masterVector = Server.prop.getProperty("masterVector", "O1*D2cox*3l@KkEH");


    private static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
//            System.out.println("encrypted string: "
//                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static String decrypt(String key, String initVector, String encrypted) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

        return new String(original);
    }

    public static String decryptAsUser(String encrypted) throws Exception {
        return decrypt(userKey, userVector, encrypted);
    }

    public static String encryptAsUser(String value) {
        return encrypt(userKey, userVector, value);
    }

    public static String decryptAsMaster(String encrypted) throws Exception {
        return decrypt(masterKey, masterVector, encrypted);
    }

    public static String encryptAsMaster(String value) {
        return encrypt(masterKey, masterVector, value);
    }

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (InputStream input = EncryptUtils.class.getClassLoader().getResourceAsStream("app.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("encrypted string: " + encrypt(masterKey, masterVector, "master"));
        System.out.println("encrypted string: " + encrypt(userKey, userVector, "master"));

//        System.out.println(decrypt(userKey, userVector, encrypt(userKey, userVector, "master")));
        System.out.println(decrypt(userKey, userVector, "VUFBjzqQSLQ9JdpeicrIeQ=="));
    }

}
