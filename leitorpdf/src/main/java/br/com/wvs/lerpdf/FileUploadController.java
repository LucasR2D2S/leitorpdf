package br.com.wvs.lerpdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.wvs.lerpdf.model.ArqCsv;
import br.com.wvs.lerpdf.model.Imagem;
import br.com.wvs.lerpdf.model.Caminhos;
import br.com.wvs.lerpdf.model.PdfCheck;
import magick.*;

import com.github.jaiimageio.impl.plugins.raw.RawImageReaderSpi;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.qoppa.pdf.PDFException;
import com.qoppa.pdfPreflight.PDFPreflight;
import com.qoppa.pdfPreflight.profiles.PDFA_1_A_Verification;
import com.qoppa.pdfPreflight.profiles.PDFA_1_B_Verification;
import com.qoppa.pdfPreflight.profiles.PDFA_2_B_Verification;
import com.qoppa.pdfPreflight.profiles.PDFA_2_U_Verification;
import com.qoppa.pdfPreflight.profiles.PDFA_3_B_Verification;
import com.qoppa.pdfPreflight.profiles.PDFA_3_B_ZUGFeRD_Verification;
import com.qoppa.pdfPreflight.results.PreflightResults;

@Controller
public class FileUploadController extends HttpServlet{

	static Logger logger = LoggerFactory.getLogger(FileUploadController.class);
	
	private static final long serialVersionUID = 1L;
	
	String folderPathTexts = System.getProperty( "catalina.base" ) + "\\webapps\\leitorpdf\\WEB-INF\\classes\\static\\Textos";
	String folderPathImgs = System.getProperty( "catalina.base" ) + "\\webapps\\leitorpdf\\WEB-INF\\classes\\static\\Imagens";
	String folderPathImgsSolo = System.getProperty( "catalina.base" ) + "\\webapps\\leitorpdf\\WEB-INF\\classes\\static\\ImagensSolo";
	String folderPathPdfs = System.getProperty( "catalina.base" ) + "\\webapps\\leitorpdf\\WEB-INF\\classes\\static\\Pdfs";
	String folderPathCsv = System.getProperty( "catalina.base" ) + "\\webapps\\leitorpdf\\WEB-INF\\classes\\static\\Csvs";
	
	private static int cont = 0;
	
	@RequestMapping("/")
	public String index(Model model) throws IOException, ImageReadException{
		
		logger.info("Iniciado carregamento da pagina inicial");
		
		Caminhos paths = new Caminhos(folderPathPdfs, folderPathImgs, folderPathTexts);
		
		model.addAttribute("paths", paths);
		
		File folderS = new File(folderPathImgsSolo);
		folderS.mkdirs();

		logger.debug("Iniciando carregamento dos PDFs");
		File folderP = new File(folderPathPdfs);
		folderP.mkdirs();
		File [] listOfFilesP = folderP.listFiles();
		model.addAttribute("filesP", listOfFilesP);
		logger.debug("PDFs carregados com sucesso!");
		
		logger.debug("Iniciando carregamento dos textos");
		File folderT = new File(folderPathTexts);
		folderT.mkdirs();
		File [] listOfFilesT = folderT.listFiles();
		model.addAttribute("filesT", listOfFilesT);
		logger.debug("Textos carregados com sucesso!");
		
		logger.debug("Iniciando carregamento das imagens");
		File folderI = new File(folderPathImgs);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());

			IIORegistry registry = IIORegistry.getDefaultInstance();
			registry.registerServiceProvider(new RawImageReaderSpi());

			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			imagens.add(imagem);
		}
		model.addAttribute("imagens", imagens);
		logger.debug("Imagens carregadas com sucesso!");
		
		logger.info("Página inicial carregada com sucesso!");
		
		return "index";
	}

	@RequestMapping("/{teste}")
	public String concluido(Model model, @PathVariable ArrayList<Boolean> teste) throws IOException, ImageReadException{
		
		logger.info("Carregando página inicial com checks");
		
		PdfCheck check = new PdfCheck();
		
		check.setImg(teste.get(0));
		check.setText(teste.get(1));
		check.setPdfa(teste.get(2));
		
		model.addAttribute("check", check);
		
		Caminhos paths = new Caminhos(folderPathPdfs, folderPathImgs, folderPathTexts);
		
		model.addAttribute("paths", paths);
		
		File folderS = new File(folderPathImgsSolo);
		folderS.mkdirs();

		logger.debug("Iniciando carregamento dos PDFs");
		File folderP = new File(folderPathPdfs);
		folderP.mkdirs();
		File [] listOfFilesP = folderP.listFiles();
		model.addAttribute("filesP", listOfFilesP);
		logger.debug("PDFs carregados com sucesso!");
		
		logger.debug("Iniciando carregamento dos textos");
		File folderT = new File(folderPathTexts);
		folderT.mkdirs();
		File [] listOfFilesT = folderT.listFiles();
		model.addAttribute("filesT", listOfFilesT);
		logger.debug("Textos carregados com sucesso!");
		
		logger.debug("Iniciando carregamento das imagens");
		File folderI = new File(folderPathImgs);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());

			IIORegistry registry = IIORegistry.getDefaultInstance();   
			registry.registerServiceProvider(new RawImageReaderSpi());            

			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			imagens.add(imagem);
		}
		
		model.addAttribute("imagens", imagens);
		logger.debug("Imagens carregadas com sucesso!");
		
		logger.info("Página inicial com checks carregada com sucesso!");
		
		return "index";
	}

	@RequestMapping("/indexImagem")
	public String indexImagem(Model model) throws IOException, ImageReadException, MagickException{
		
		logger.info("Carregando página de imagem");
		
		Caminhos paths = new Caminhos(folderPathImgsSolo);
		
		model.addAttribute("paths", paths);

		File folderI = new File(folderPathImgsSolo);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());

			IIORegistry registry = IIORegistry.getDefaultInstance();   
			registry.registerServiceProvider(new RawImageReaderSpi());

			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			if (imagem.getMimetype().contentEquals("image/jpeg") || imagem.getMimetype().contentEquals("image/tiff")) {
				if (imagem.getDpih()==200 || imagem.getDpih()==300) {
					if (imagem.getMimetype().contentEquals("image/jpeg")) {
						if (imagem.getCor().contentEquals("Tons de Cinza")) {
							imagem.setGcc("Sim");
							imagem.setObs("");
						}
						else {
							imagem.setGcc("Nao");
							imagem.setObs("Imagem nao é tons de cinza, ela possui " + imageInfo.getBitsPerPixel() + " a tornando " + imagem.getCor());
						}
					}
					else {
						imagem.setGcc("Nao");
						imagem.setObs("Imagem de tipo " + imagem.getFormat() + ", imagens Jpeg ou Tiff são o ideal.");
					}
					if (imagem.getMimetype().contentEquals("image/tiff")) {
						if (imagem.getComp().contentEquals("CCITT Group 4")) {
							if (imagem.getCor().contentEquals("Preto e Branco")) {
								imagem.setGcc("Sim");
								imagem.setObs("");
							}
							else {
								imagem.setGcc("Nao");
								imagem.setObs("Imagem nao é preto e branco, ela possui " + imageInfo.getBitsPerPixel() + " a tornando " + imagem.getCor() + ".");
							}
						}
						else {
							imagem.setGcc("Nao");
							imagem.setObs("Imagem com compactação " + imagem.getComp() + ". O ideal é a CCITT Group 4 para as Tiffs e JPEG para as Jpegs.");
						}
					}
					else {
						imagem.setGcc("Nao");
						imagem.setObs("Imagem de tipo " + imagem.getFormat() + ", imagens Jpeg ou Tiff sao o ideal.");
					}	
				}
				else {
					imagem.setGcc("Nao");
					imagem.setObs("A imagem possui um DPI de " + imagem.getDpih() + ", o ideal deveria ser 200dpi ou 300dpi.");
				}
			}
			else {
				imagem.setGcc("Nao");
				imagem.setObs("Imagem de tipo " + imagem.getFormat() + ", imagens Jpeg ou Tiff sao o ideal.");
			}
			imagens.add(imagem);
		}
		
		model.addAttribute("imagens", imagens);
		
		logger.info("Página de imagem carregada com sucesso!");
		
		return "indexImagem";
	}
	
	@RequestMapping("/indexImagens")
	public String indexImagens(Model model) throws IOException, ImageReadException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException{
		
		logger.info("Carregando página para imagens");
		
		Caminhos paths = new Caminhos(folderPathImgsSolo, folderPathCsv);
		
		String nomeCsv;
		String formatCsv;
		String tamanhoCsv;
		String alturalarguraCsv;
		String mimetypeCsv;
		String compCsv;
		String corCsv;
		String gccCsv;
		String obsCsv;
		
		ArqCsv val = new ArqCsv();
		
		List<ArqCsv> infos = new ArrayList<>();
		
		File folderC = new File(folderPathCsv);
		folderC.mkdirs();
		
		model.addAttribute("paths", paths);
		
		File folderI = new File(folderPathImgsSolo);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());
			
			nomeCsv = listOfFilesI[i].getName();
			
			IIORegistry registry = IIORegistry.getDefaultInstance();   
			registry.registerServiceProvider(new RawImageReaderSpi());
			
			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			
			alturalarguraCsv = h + " x " + w;
			
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			
			tamanhoCsv = tam + "KB";
			
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			
			mimetypeCsv = imageInfo.getMimeType();
			
			formatCsv = imageInfo.getMimeType();
			
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			System.out.println(imageInfo.toString());
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			
			corCsv = imagem.getCor();
			
			compCsv = imagem.getComp();
			
			imagens.add(imagem);
			
			if (imagem.getMimetype().contentEquals("image/jpeg") || imagem.getMimetype().contentEquals("image/tiff")) {
				if (imagem.getDpih()==200 || imagem.getDpih()==300) {
					if (imagem.getMimetype().contentEquals("image/jpeg")) {
						if (imagem.getCor().contentEquals("Tons de Cinza")) {
							gccCsv = "Sim";
							obsCsv = "Tudo joia";
						}
						else {
							gccCsv = "Não";
							obsCsv = "Imagem colorida";
						}
					}
					else {
						gccCsv = "Não";
						obsCsv = "Imagem de tipo " + imagem.getFormat();
					}
					if (imagem.getMimetype().contentEquals("image/tiff")) {
						if (imagem.getComp().contentEquals("CCITT Group 4")) {
							if (imagem.getCor().contentEquals("Preto e Branco")) {
								gccCsv = "Sim";
								obsCsv = "Tudo joia";
							}
							else {
								gccCsv = "Não";
								obsCsv = "Imagem não é preto e branco, ela possui " + imageInfo.getBitsPerPixel() + " a tornando " + imagem.getCor();
							}
						}
						else {
							gccCsv = "Não";
							obsCsv = "Imagem com a compactação " + imagem.getMimetype();
						}
					}
					else {
						gccCsv = "Não";
						obsCsv = "Imagem de tipo " + imagem.getFormat();
					}	
				}
				else {
					gccCsv = "Não";
					obsCsv = "A imagem possui o DPI de " + imagem.getDpih();
				}
			}
			else {
				gccCsv = "Não";
				obsCsv = "Imagem de tipo " + imagem.getFormat();
			}
			infos.add(new ArqCsv(nomeCsv,formatCsv,tamanhoCsv,alturalarguraCsv,mimetypeCsv,compCsv,corCsv,gccCsv,obsCsv));
		}
		cont++;
		
		Writer writer = Files.newBufferedWriter(Paths.get(folderPathCsv + "\\Imagens_Geradas_" + cont + ".csv"));
        StatefulBeanToCsv<ArqCsv> beanToCsv = new StatefulBeanToCsvBuilder<ArqCsv>(writer).build();
        
        beanToCsv.setOrderedResults(true);
        
        beanToCsv.write(infos);
        
        beanToCsv.setOrderedResults(true);
        
        writer.flush();
        
        writer.close();
		
        String testeNome = "Imagens_Geradas_" + cont + ".csv";
        
        val.setNomeCsv(testeNome);
        
        model.addAttribute("valida", val);
        
		model.addAttribute("imagens", imagens);
		
		model.addAttribute("infos", infos);
		
		logger.info("Página de imagem carregada com sucesso!");
		
		return "indexImagens";
	}
	
	@RequestMapping("/retornoI")
	public String retornaIndex(Model model) throws ImageReadException, IOException{

		logger.info("Retornando a página inicial");
		
		Caminhos paths = new Caminhos(folderPathPdfs, folderPathImgs, folderPathTexts);
		
		model.addAttribute("paths", paths);
		
		File folderS = new File(folderPathImgsSolo);
		folderS.mkdirs();

		logger.debug("Iniciando carregamento dos PDFs");
		File folderP = new File(folderPathPdfs);
		folderP.mkdirs();
		File [] listOfFilesP = folderP.listFiles();
		model.addAttribute("filesP", listOfFilesP);
		logger.debug("PDFs carregados com sucesso!");
		
		logger.debug("Iniciando carregamento dos textos");
		File folderT = new File(folderPathTexts);
		folderT.mkdirs();
		File [] listOfFilesT = folderT.listFiles();
		model.addAttribute("filesT", listOfFilesT);
		logger.debug("Textos carregados com sucesso!");
		
		logger.debug("Iniciando carregamento das imagens");
		File folderI = new File(folderPathImgs);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());

			IIORegistry registry = IIORegistry.getDefaultInstance();   
			registry.registerServiceProvider(new RawImageReaderSpi());            

			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			imagens.add(imagem);
		}
		
		model.addAttribute("imagens", imagens);
		logger.debug("Imagens carregadas com sucesso!");
		
		logger.info("Retorno feito com sucesso");
		
		return "index";
	}

	@RequestMapping("/deleteP")
	public String deletePdf(Model model) throws IOException, ImageReadException{

		logger.info("Iniciando serviço para deletar os PDFs");
		
		File listaPdf = new File(folderPathPdfs);
		FileUtil.deleteContents(listaPdf);

		logger.debug("PDFs deletados");
		
		Caminhos paths = new Caminhos(folderPathPdfs, folderPathImgs, folderPathTexts);
		
		model.addAttribute("paths", paths);
		
		File folderS = new File(folderPathImgsSolo);
		folderS.mkdirs();

		logger.debug("Iniciando carregamento dos PDFs");
		File folderP = new File(folderPathPdfs);
		folderP.mkdirs();
		File [] listOfFilesP = folderP.listFiles();
		model.addAttribute("filesP", listOfFilesP);
		logger.debug("PDFs carregados com sucesso!");
		
		logger.debug("Iniciando carregamento dos textos");
		File folderT = new File(folderPathTexts);
		folderT.mkdirs();
		File [] listOfFilesT = folderT.listFiles();
		model.addAttribute("filesT", listOfFilesT);
		logger.debug("Textos carregados com sucesso!");
		
		logger.debug("Iniciando carregamento das imagens");
		File folderI = new File(folderPathImgs);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());

			IIORegistry registry = IIORegistry.getDefaultInstance();   
			registry.registerServiceProvider(new RawImageReaderSpi());            

			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			imagens.add(imagem);
		}

		model.addAttribute("imagens", imagens);
		logger.debug("Imagens carregadas com sucesso!");
		
		logger.info("Serviço de deletar os PDFs concluído com sucesso");
		
		return "index";
	}
	
	@RequestMapping("/indexErroUploadImagem")
	public String erroImagem(Model model) throws IOException, ImageReadException{
		
		logger.warn("Iniciado carregamento da pagina inicial, apos erro no upload");
		logger.info("Iniciado carregamento da pagina inicial");
		
		Caminhos paths = new Caminhos(folderPathPdfs, folderPathImgs, folderPathTexts);
		
		model.addAttribute("paths", paths);
		
		File folderS = new File(folderPathImgsSolo);
		folderS.mkdirs();

		logger.debug("Iniciando carregamento dos PDFs");
		File folderP = new File(folderPathPdfs);
		folderP.mkdirs();
		File [] listOfFilesP = folderP.listFiles();
		model.addAttribute("filesP", listOfFilesP);
		logger.debug("PDFs carregados com sucesso!");
		
		logger.debug("Iniciando carregamento dos textos");
		File folderT = new File(folderPathTexts);
		folderT.mkdirs();
		File [] listOfFilesT = folderT.listFiles();
		model.addAttribute("filesT", listOfFilesT);
		logger.debug("Textos carregados com sucesso!");
		
		logger.debug("Iniciando carregamento das imagens");
		File folderI = new File(folderPathImgs);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());

			IIORegistry registry = IIORegistry.getDefaultInstance();   
			registry.registerServiceProvider(new RawImageReaderSpi());            

			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			imagens.add(imagem);
		}
		model.addAttribute("imagens", imagens);
		logger.debug("Imagens carregadas com sucesso!");
		
		logger.info("Página inicial carregada com sucesso!");

		return "indexErroUploadImagem";
	}
	
	@RequestMapping("/indexErroUploadPdf")
	public String erroPdf(Model model) throws IOException, ImageReadException{
		
		logger.warn("Iniciado carregamento da pagina inicial, apos erro no upload");
		logger.info("Iniciado carregamento da pagina inicial");
		
		Caminhos paths = new Caminhos(folderPathPdfs, folderPathImgs, folderPathTexts);
		
		model.addAttribute("paths", paths);
		
		File folderS = new File(folderPathImgsSolo);
		folderS.mkdirs();

		logger.debug("Iniciando carregamento dos PDFs");
		File folderP = new File(folderPathPdfs);
		folderP.mkdirs();
		File [] listOfFilesP = folderP.listFiles();
		model.addAttribute("filesP", listOfFilesP);
		logger.debug("PDFs carregados com sucesso!");
		
		logger.debug("Iniciando carregamento dos textos");
		File folderT = new File(folderPathTexts);
		folderT.mkdirs();
		File [] listOfFilesT = folderT.listFiles();
		model.addAttribute("filesT", listOfFilesT);
		logger.debug("Textos carregados com sucesso!");
		
		logger.debug("Iniciando carregamento das imagens");
		File folderI = new File(folderPathImgs);
		folderI.mkdirs();
		File [] listOfFilesI = folderI.listFiles();
		ArrayList<Imagem> imagens = new ArrayList<Imagem>();
		for (int i = 0; i<listOfFilesI.length; i++){
			Imagem imagem = new Imagem();
			imagem.setNome(listOfFilesI[i].getName());

			IIORegistry registry = IIORegistry.getDefaultInstance();   
			registry.registerServiceProvider(new RawImageReaderSpi());            

			BufferedImage img = ImageIO.read(listOfFilesI[i]);
			int h = img.getHeight();
			int w = img.getWidth();
			imagem.setAltura(h);
			imagem.setLargura(w);
			double tamP = 1024.00;
			double tam = listOfFilesI[i].length()/tamP;
			imagem.setTamanho(tam);
			ImageInfo imageInfo = Sanselan.getImageInfo(listOfFilesI[i]);
			float dpiWidth = imageInfo.getPhysicalWidthDpi();
			float dpiHeight = imageInfo.getPhysicalHeightDpi();
			float dpi = (dpiWidth*dpiHeight);
			imagem.setMimetype(imageInfo.getMimeType());
			imagem.setFormat(imageInfo.getFormatName());
			imagem.setDpiw(dpiWidth);
			imagem.setDpih(dpiHeight);
			imagem.setDpi(dpi);
			String testeString = imageInfo.toString();
			int numCome = imageInfo.getComments().size() + 5;
			int numTran = imageInfo.getComments().size() + 15;
			int bits = imageInfo.getBitsPerPixel();
			if (imageInfo.getMimeType().contentEquals("image/png")){
				byte[] fileContent = Files.readAllBytes(listOfFilesI[i].toPath());
				int bit25 = fileContent[25];
				if ((bit25 == 0) || (bit25 == 4)){
					imagem.setCor("Tons de Cinza");
				}
				else{
					imagem.setCor("Colorida");
				}
			}
			else if (imageInfo.getMimeType().contentEquals("image/gif")){
				String[] partsT = testeString.split(System.getProperty("line.separator"));
				String gab = partsT[numTran].replace("Is Transparent: ", "");
				if (gab.contentEquals("true")){
					imagem.setCor("Colorida");
				}
				else{
					imagem.setCor("Tons de cinza");
				}
			}
			else if (bits == 1){
				imagem.setCor("Preto e Branco");
			}
			else if (bits == 8){
				imagem.setCor("Tons de Cinza");	
			}
			else{
				imagem.setCor("Colorida");
			}
			String[] parts = testeString.split(System.getProperty("line.separator"));
			imagem.setComp(parts[numCome].replace("Compression Algorithm: ", ""));
			imagens.add(imagem);
		}
		model.addAttribute("imagens", imagens);
		logger.debug("Imagens carregadas com sucesso!");
		
		logger.info("Página inicial carregada com sucesso!");

		return "indexErroUploadPdf";
	}
	
	@PostMapping("postaimagem")
	public String imagemRequest(@RequestParam("file") MultipartFile[] file, RedirectAttributes redirectAttributes, Model model) {
		
		System.gc();
		
		for (int i=0; i<file.length;i++) {
			String typeTeste = file[i].getContentType();
	
			//Verificando se o file que esta entrando e uma imagem ou nao
			if(typeTeste.contentEquals("application/pdf")){
				logger.warn("Arquivo identificado como pdf, quando se esperava uma imagem");
				return "redirect:/indexErroUploadImagem";
			}
		}
		
		logger.info("Iniciando POST e analise de imagem(ns)");
		File listaImgSolo = new File(folderPathImgsSolo);
		FileUtil.deleteContents(listaImgSolo);
		if (file.length==1) {
			logger.debug("Identificado apenas uma imagem de entrada");
			logger.debug("Imagens antigas deletadas");
			File fileTeste = new File(folderPathImgsSolo + "\\" + file[0].getOriginalFilename());
			try {
				file[0].transferTo(fileTeste.getAbsoluteFile());
			} catch (IllegalStateException | IOException e) {
				logger.error("Erro ao copiar arquivo. Erro: ", e);
			}
			logger.debug("Arquivo copiado com sucesso");
			logger.info("POST e analise concluidos com sucesso");
			return "redirect:/indexImagem";
		}
		else {
			for (int i=0; i<file.length;i++) {
				File fileTeste = new File(folderPathImgsSolo + "\\" + file[i].getOriginalFilename());
				try {
					file[i].transferTo(fileTeste.getAbsoluteFile());
				} catch (IllegalStateException | IOException e) {
					logger.error("Erro ao copiar arquivo. Erro: ", e);
				}
			}
			logger.debug("Identificado multiplas imagens como entrada");
			logger.info("POST e analise concluidos com sucesso");
			return "redirect:/indexImagens";
		}
	}
	
	@PostMapping("postando")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes, Model model) throws InvalidPasswordException, IOException, PDFException, ImageReadException {

		logger.info("Serviço de upload iniciado");
		
		logger.debug("POST iniciado");
		
		boolean checkT = false;
		boolean checkI;
		boolean checkP;

		System.gc();
		
//		if (file.getName().endsWith("jpg")||file.getName().endsWith("png")||file.getName().endsWith("gif")||file.getName().endsWith("bmp")||file.getName().endsWith("tiff")||file.getName().endsWith("tif")||file.getName().endsWith("jpeg")||file.getName().endsWith("TIF")) {
//			return "redirect:/indexErroUploadPdf";
//		}
		
		String typeTeste = file.getContentType();

		//Verificando se o file que esta entrando e uma imagem ou nao
		if(typeTeste.contentEquals("image/gif") ||  
				typeTeste.contentEquals("image/jpeg") || 
				typeTeste.contentEquals("image/png") || 
				typeTeste.contentEquals("image/jpg") || 
				typeTeste.contentEquals("image/tiff") ||
				typeTeste.contentEquals("image/tif") ||
				typeTeste.contentEquals("image/TIF") ||
				typeTeste.contentEquals("image/x-windows-bmp") ||
				typeTeste.contentEquals("image/bmp") ||
				typeTeste.contentEquals("image/pjpeg")){
			logger.warn("Arquivo identificado como imagem, quando se esperava um PDF");
			return "redirect:/indexErroUploadPdf";
		}
		
		logger.debug("Arquivo identificado como PDF");

		File listaTxt = new File(folderPathTexts);
		FileUtil.deleteContents(listaTxt);
		logger.debug("Textos antigos deletados");
		File listaImg = new File(folderPathImgs);
		FileUtil.deleteContents(listaImg);
		logger.debug("Imagens antigas deletadas");
		
		try {
		PDDocument doc = PDDocument.load(file.getInputStream());
		PDFTextStripper pdfTextStripper = new PDFTextStripper();
		if (pdfTextStripper.getText(doc).isEmpty()){
			logger.debug("PDF não possui textos");
			checkT = false;
		}
		else{
			String PDFText =  pdfTextStripper.getText(doc);
			if (!PDFText.trim().isEmpty()){
				logger.debug("PDF possui textos");
				String nomePDFText = file.getOriginalFilename();
				nomePDFText = nomePDFText.replace(".pdf", "");
				File arqTexto = new File(folderPathTexts, nomePDFText + ".txt");
				FileUtils.writeStringToFile(arqTexto, PDFText, StandardCharsets.ISO_8859_1);
				checkT = true;
			}
			else{
				checkT = false;
			}
		}
		} catch(IOException e) {
			logger.warn("Nao foi possivel identificar se o PDF possui textos ou não. Erro: " + e);
		}
		
		String nomePDFText = file.getOriginalFilename();
		nomePDFText = nomePDFText.replace(".pdf", ""); 
		
		checkP = false;
		MultipartFile copia = file;
		File copiaTeste = new File(folderPathPdfs, file.getOriginalFilename());
		Path dest = copiaTeste.toPath();
		copia.transferTo(dest);
		PDFPreflight pdfPreflight = new PDFPreflight(folderPathPdfs + "\\" + file.getOriginalFilename(), null);
						
		// Verificando se o documento e pdfa
		PreflightResults results1A = pdfPreflight.verifyDocument(new PDFA_1_A_Verification(), null);
		PreflightResults results1B = pdfPreflight.verifyDocument(new PDFA_1_B_Verification(), null);
		PreflightResults results2B = pdfPreflight.verifyDocument(new PDFA_2_B_Verification(), null);
		PreflightResults results2U = pdfPreflight.verifyDocument(new PDFA_2_U_Verification(), null);
		PreflightResults results3B = pdfPreflight.verifyDocument(new PDFA_3_B_Verification(), null);
		PreflightResults results3BZ = pdfPreflight.verifyDocument(new PDFA_3_B_ZUGFeRD_Verification(), null);
		
		logger.debug("Iniciando checagem de PDF/A");
		
		//Pegando os resultados
		if (results1A.isSuccessful()){
			logger.debug("Arquivo identificado como PDF/A");
			checkP = true;
		}
		else if (results1B.isSuccessful()){
			logger.debug("Arquivo identificado como PDF/A");
			checkP = true;
		}
		else if (results2B.isSuccessful()){
			logger.debug("Arquivo identificado como PDF/A");
			checkP = true;
		}
		else if (results2U.isSuccessful()){
			logger.debug("Arquivo identificado como PDF/A");
			checkP = true;
		}
		else if (results3B.isSuccessful()){
			logger.debug("Arquivo identificado como PDF/A");
			checkP = true;
		}
		else if (results3BZ.isSuccessful()){
			logger.debug("Arquivo identificado como PDF/A");
			checkP = true;
		}
		else {
			logger.warn("Arquivo não é formatado em PDF/A ou não pode ser identificado pelo serviço");
		}
		//Verificando se o pdf possui imagens e se tiver extrai
		checkI = false;
		logger.debug("Iniciando a verificação se o PDF possui imagens");
		PDDocument doc = PDDocument.load(file.getInputStream());
		for (int pageCounter=0; pageCounter < doc.getNumberOfPages(); pageCounter++) {    

			PDPage pdPage = doc.getPage(pageCounter);

			PDResources pdResources = pdPage.getResources();

			int imageCounter = 1;

			for (COSName name : pdResources.getXObjectNames()) {

				PDXObject o = pdResources.getXObject(name);

				if (o instanceof PDImageXObject) {
					logger.debug("Extraindo imagem");
					checkI = true;
					PDImageXObject image = (PDImageXObject) o;
					ImageIO.write(image.getImage(), image.getSuffix(), new File(folderPathImgs + "\\Imagem_"
							+ pageCounter + "_" + imageCounter + nomePDFText + "." + image.getSuffix()));
					imageCounter++;
					logger.debug("Imagem extraída");
				}

				if (o instanceof PDFormXObject) {

					PDResources formResources = ((PDFormXObject) o).getResources();

					for (COSName formObjname : formResources.getXObjectNames()) {
						PDXObject of = pdResources.getXObject(formObjname);

						if (of instanceof PDImageXObject) {
							logger.info("Extraindo imagem");
							checkI = true;
							PDImageXObject image = (PDImageXObject) of;
							ImageIO.write(image.getImage(), image.getSuffix(), new File(folderPathImgs + "\\Imagem_"
									+ pageCounter + "_" + imageCounter + nomePDFText + "." + image.getSuffix()));
							imageCounter++;
							logger.debug("Imagem extraída");
						}		
					}
				}
			}
		}
		doc.close();
		
		//Preparando os checks que serao enviados
		logger.debug("Carregando o model check");
		PdfCheck checkValid = new PdfCheck(checkI, checkP, checkT);
		
		model.addAttribute("check", checkValid);

		ArrayList<Boolean> teste = new ArrayList<Boolean>();

		teste.add(checkValid.getImg());
		teste.add(checkValid.getText());
		teste.add(checkValid.getPdfa());

		redirectAttributes.addAttribute("teste",teste);
		logger.debug("Model check carregado com sucesso!");
		
		logger.debug("POST concluido com sucesso");
		
		logger.info("Serviço concluido com sucesso");
		
		return "redirect:/{teste}";
	}    
}