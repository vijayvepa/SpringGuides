package com.example.webfluxexample.health.web;

import com.example.webfluxexample.health.config.SslConfigProperties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import javax.net.ssl.SSLException;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

public class SecureWebClient {
  /**
   * Gets insecure trust web client.
   *
   * @param url the url
   * @return the insecure trust web client
   */

  public static WebClient getInsecureTrustWebClient(final String url) {
    try {
      final SslContext context = SslContextBuilder.forClient()
          .trustManager(InsecureTrustManagerFactory.INSTANCE)
          .build();

      final HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(context));

      return WebClient
          .builder()
          .baseUrl(url)
          .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    } catch (SSLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets keystore trust web client.
   *
   * @param url the url
   * @param sslConfigProperties the ssl config properties
   * @return the keystore trusted web client
   */

  @SuppressWarnings("unused") //will be used when ssl cert is available
  public static WebClient getKeyStoreTrustWebClient(final String url, final SslConfigProperties sslConfigProperties) {

    try {
      final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(new FileInputStream(ResourceUtils.getFile(sslConfigProperties.trustStorePath())),
          sslConfigProperties.trustStorePassword().toCharArray());
      final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(new FileInputStream(ResourceUtils.getFile(sslConfigProperties.keyAlias())),
          sslConfigProperties.trustStorePassword().toCharArray());

      final X509Certificate[] trustStoreCertificates = Collections.list(trustStore.aliases()).stream()
          .filter(alias -> isCertificateEntry(alias, trustStore))
          .map(alias -> getCertificate(alias, trustStore))
          .map(X509Certificate.class::cast)
          .toList()
          .toArray(new X509Certificate[0]);

      final PrivateKey privateKey = (PrivateKey) keyStore.getKey(sslConfigProperties.keyAlias(), sslConfigProperties.keyStorePassword().toCharArray());
      final Certificate[] certChain = keyStore.getCertificateChain(sslConfigProperties.keyAlias());
      final X509Certificate[] keyStoreCertificates = Arrays.stream(certChain)
          .map(X509Certificate.class::cast)
          .toList()
          .toArray(new X509Certificate[0]);

      final SslContext sslContext = SslContextBuilder.forClient()
          .keyManager(privateKey, sslConfigProperties.keyStorePassword(), keyStoreCertificates)
          .trustManager(trustStoreCertificates)
          .build();

      final HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

      return WebClient
          .builder()
          .baseUrl(url)
          .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (CertificateException e) {
      throw new RuntimeException(e);
    } catch (UnrecoverableKeyException e) {
      throw new RuntimeException(e);
    }
  }


  private static Certificate getCertificate(final String alias, final KeyStore trustStore) {
    try {
      return trustStore.getCertificate(alias);
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }


  private static boolean isCertificateEntry(final String alias, final KeyStore trustStore) {
    try {
      return trustStore.isCertificateEntry(alias);
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }
}
