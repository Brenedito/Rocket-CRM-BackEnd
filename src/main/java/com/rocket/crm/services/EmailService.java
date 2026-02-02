package com.rocket.crm.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void enviarCredenciais(String email, String nome, String senha) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Acesso Garantido // RocketCompany");

            String htmlContent = """
            <div style="background-color: #020617; color: #f1f5f9; font-family: 'Montserrat', sans-serif; padding: 40px; border: 1px solid #1e293b; max-width: 600px;">
                <h1 style="color: #22d3ee; text-transform: uppercase; letter-spacing: 2px; border-bottom: 2px solid #3b82f6; padding-bottom: 10px;">
                    Access Granted_
                </h1>
                <p style="font-size: 16px; line-height: 1.6;">
                    Olá <strong>%s</strong>, Sua conta operacional agora está ativa no <strong>RocketCompany CRM</strong>.
                </p>
                <div style="background-color: #0f172a; border-left: 4px solid #22d3ee; padding: 20px; margin: 25px 0;">
                    <p style="margin: 0; color: #94a3b8; font-size: 12px; text-transform: uppercase;">Credenciais:</p>
                    <p style="margin: 10px 0 5px 0;"><strong>Login:</strong> %s</p>
                    <p style="margin: 0;"><strong>Senha:</strong> <span style="color: #22d3ee;">%s</span></p>
                </div>
                <a href="n8n.rocketcompanny.com.br" style="display: inline-block; background-color: #3b82f6; color: #ffffff; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 2px; text-transform: uppercase; font-size: 14px;">
                    Acesse agora!
                </a>
                <p style="margin-top: 30px; font-size: 12px; color: #475569;">
                    // Nota de Segurança: Esta é uma chave temporária criptografada. A política do sistema exige a rotação imediata da senha após o primeiro acesso.
                </p>
                <hr style="border: 0; border-top: 1px solid #1e293b; margin-top: 40px;">
                <p style="text-align: center; color: #3b82f6; font-size: 10px; letter-spacing: 4px;">ROCKETCOMPANY // ROCKET-CRM</p>
            </div>
        """.formatted(nome, email, senha);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao descriptografar envio de e-mail: " + e.getMessage());
        }
    }
}
// Alterar no Futuro