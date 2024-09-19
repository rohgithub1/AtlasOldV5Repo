package com.cci.rest.service.mail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class AtlasMailService {
	
	@Autowired
	private JavaMailSender sender;

	public AtlasMailResponse sendEmail(AtlasMailRequest atlasMailRequest) throws ParseException {
		AtlasMailResponse response = new AtlasMailResponse();

		String attachmentName = atlasMailRequest.getAttachment();

		FileSystemResource file = new FileSystemResource(new File("C:\\Atlas_Daily_Report\\" + attachmentName));

		MimeMessage message = sender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());

			helper.setTo(atlasMailRequest.getTo());
			helper.setCc(atlasMailRequest.getCc());
			helper.setSubject(atlasMailRequest.getSubject().trim());
			helper.setFrom(atlasMailRequest.getFrom().trim());
			helper.setText(atlasMailRequest.getBody(), true);
			helper.addAttachment(attachmentName, file);

			sender.send(message);
			System.out.println("Sending Mail!!");

			response.setMessage("mail send to : " + atlasMailRequest.getTo());
			response.setStatus(Boolean.TRUE);
			System.out.println("Mail Sent!");

		} catch (MessagingException e) {
			e.printStackTrace();
			response.setMessage("Mail Sending failure : " + e.getMessage());
			response.setStatus(Boolean.FALSE);
		}

		return response;
	}
}
