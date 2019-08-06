package br.com.wvs.lerpdf.model;

public class ArqCsv {
	
	private String nomeCsv;
	private String formatCsv;
	private String tamanhoCsv;
	private String alturalarguraCsv;
	private String mimetypeCsv;
	private String compCsv;
	private String corCsv;
	private String gccCsv;
	private String obsCsv;
	
	public ArqCsv() {
		
	}
	
	public ArqCsv(String nomeCsv, String formatCsv, String tamanhoCsv, String alturalarguraCsv, String mimetypeCsv, String compCsv, String corCsv, String gccCsv, String obsCsv) {
		this.nomeCsv = nomeCsv;
		this.formatCsv = formatCsv;
		this.tamanhoCsv = tamanhoCsv;
		this.alturalarguraCsv = alturalarguraCsv;
		this.mimetypeCsv = mimetypeCsv;
		this.compCsv = compCsv;
		this.corCsv = corCsv;
		this.gccCsv = gccCsv;
		this.obsCsv = obsCsv;
	}
	
	public String getNomeCsv() {
		return nomeCsv;
	}
	public void setNomeCsv(String nomeCsv) {
		this.nomeCsv = nomeCsv;
	}
	public String getFormatCsv() {
		return formatCsv;
	}
	public void setFormatCsv(String formatCsv) {
		this.formatCsv = formatCsv;
	}
	public String getGccCsv() {
		return gccCsv;
	}
	public void setGccCsv(String gccCsv) {
		this.gccCsv = gccCsv;
	}
	public String getObsCsv() {
		return obsCsv;
	}
	public void setObsCsv(String obsCsv) {
		this.obsCsv = obsCsv;
	}

	public String getTamanhoCsv() {
		return tamanhoCsv;
	}

	public void setTamanhoCsv(String tamanhoCsv) {
		this.tamanhoCsv = tamanhoCsv;
	}

	public String getAlturaLarguraCsv() {
		return alturalarguraCsv;
	}

	public void setAlturaCsv(String alturalarguraCsv) {
		this.alturalarguraCsv = alturalarguraCsv;
	}

	public String getMimetypeCsv() {
		return mimetypeCsv;
	}

	public void setMimetypeCsv(String mimetypeCsv) {
		this.mimetypeCsv = mimetypeCsv;
	}

	public String getCompCsv() {
		return compCsv;
	}

	public void setCompCsv(String compCsv) {
		this.compCsv = compCsv;
	}

	public String getCorCsv() {
		return corCsv;
	}

	public void setCorCsv(String corCsv) {
		this.corCsv = corCsv;
	}

	public String getAlturalarguraCsv() {
		return alturalarguraCsv;
	}

	public void setAlturalarguraCsv(String alturalarguraCsv) {
		this.alturalarguraCsv = alturalarguraCsv;
	}
	
}
