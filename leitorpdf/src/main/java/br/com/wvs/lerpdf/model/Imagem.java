package br.com.wvs.lerpdf.model;

public class Imagem {
	
	private String nome;
	private double tamanho;
	private int altura;
	private int largura;
	private float dpi;
	private float dpih;
	private float dpiw;
	private String mimetype;
	private String format;
	private String comp;
	private String cor;
	private String gcc;
	private String obs;
	
	public Imagem(){
		
	}
	
	public Imagem(String nome, int tamanho, int altura, int largura){
		this.nome = nome;
		this.tamanho = tamanho;
		this.altura = altura;
		this.largura = largura;
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public double getTamanho() {
		return tamanho;
	}
	public void setTamanho(double l) {
		this.tamanho = l;
	}
	public int getAltura() {
		return altura;
	}
	public void setAltura(int altura) {
		this.altura = altura;
	}
	public int getLargura() {
		return largura;
	}
	public void setLargura(int largura) {
		this.largura = largura;
	}
	
	public float getDpi(){
		return dpi;
	}

	public void setDpi(float dpi) {
		this.dpi = dpi;
	}

	public float getDpih() {
		return dpih;
	}

	public void setDpih(float dpih) {
		this.dpih = dpih;
	}

	public float getDpiw() {
		return dpiw;
	}

	public void setDpiw(float dpiw) {
		this.dpiw = dpiw;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getComp() {
		return comp;
	}

	public void setComp(String comp) {
		this.comp = comp;
	}

	public String getCor() {
		return cor;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}

	public String getGcc() {
		return gcc;
	}

	public void setGcc(String gcc) {
		this.gcc = gcc;
	}

	public String getObs() {
		return obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}
}
