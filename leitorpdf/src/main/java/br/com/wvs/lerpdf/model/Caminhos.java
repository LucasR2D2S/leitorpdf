package br.com.wvs.lerpdf.model;

public class Caminhos {
	
	private String pathPdf;
	private String pathImagem;
	private String pathTexto;
	private String pathImagemSolo;
	private String pathCsv;
	
	public Caminhos() {
		
	}
	
	public Caminhos(String pathPdf, String pathImagem, String pathTexto) {
		this.pathPdf = pathPdf;
		this.pathImagem = pathImagem;
		this.pathTexto = pathTexto;
	}
	
	public Caminhos(String pathImagemSolo) {
		this.pathImagemSolo = pathImagemSolo;
	}
	
	public Caminhos(String pathImagemSolo, String pathCsv) {
		this.pathImagemSolo = pathImagemSolo;
		this.pathCsv = pathCsv;
	}
	
	public String getPathPdf() {
		return pathPdf;
	}
	public void setPathPdf(String pathPdf) {
		this.pathPdf = pathPdf;
	}
	public String getPathImagem() {
		return pathImagem;
	}
	public void setPathImagem(String pathImagem) {
		this.pathImagem = pathImagem;
	}
	public String getPathTexto() {
		return pathTexto;
	}
	public void setPathTexto(String pathTexto) {
		this.pathTexto = pathTexto;
	}
	public String getPathImagemSolo() {
		return pathImagemSolo;
	}
	public void setPathImagemSolo(String pathImagemSolo) {
		this.pathImagemSolo = pathImagemSolo;
	}

	public String getPathCsv() {
		return pathCsv;
	}

	public void setPathCsv(String pathCsv) {
		this.pathCsv = pathCsv;
	}
}
