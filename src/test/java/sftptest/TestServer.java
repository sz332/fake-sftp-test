package sftptest;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.stefanbirkner.fakesftpserver.rule.FakeSftpServerRule;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class TestServer {

    private static final int PORT = 9999;
    private static final String USERNAME       = "john";
    private static final String KEY_PASSPHRASE = "unittest";

    private static Path         PRIVATE_KEY;
    private static Path         PUBLIC_KEY;

    static {
        try {
            PRIVATE_KEY = Paths.get(TestServer.class.getResource("/keys/dummy_key").toURI());
            PUBLIC_KEY = Paths.get(TestServer.class.getResource("/keys/dummy_key.pub").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Rule
    public final FakeSftpServerRule sftpServer =
            new FakeSftpServerRule().setPort(PORT).addIdentity(USERNAME, PUBLIC_KEY);

    @Test
    public void test001() throws Exception {
        JSch jsch = new JSch();

        byte[] privateKey = Files.readAllBytes(PRIVATE_KEY);
        byte[] publicKey = Files.readAllBytes(PUBLIC_KEY);

        jsch.addIdentity(USERNAME, privateKey, publicKey, KEY_PASSPHRASE.getBytes());

        Session session = jsch.getSession(USERNAME, "localhost", PORT);
        session.setConfig("StrictHostKeyChecking", "no");

        System.out.println("Establishing Connection...");
        session.connect();

        System.out.println("Connection established.");
        System.out.println("Creating SFTP Channel.");

        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        
        sftpChannel.put( new ByteArrayInputStream( "".getBytes() ), "tempfile");
        
        sftpChannel.disconnect();
        session.disconnect();
        
        boolean fileCreated = sftpServer.existsFile("/tempfile");
        Assert.assertTrue(fileCreated);
    }

}
