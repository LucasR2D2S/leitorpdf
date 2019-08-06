package br.com.wvs.lerpdf.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PdfCheck{
	

	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private int codigo;
	
	private boolean img;
	private boolean pdfa;
	private boolean text;
	
	public PdfCheck(){
		
	}
	
	public PdfCheck(boolean img,boolean pdfa, boolean text){
		this.img = img;
		this.pdfa = pdfa;
		this.text = text;
	}
	
	public void setText(boolean text){
		this.text = text;
	}
	
	public boolean getText(){
		return text;
	}
	
	public void setImg(boolean img){
		this.img = img;
	}
	
	public boolean getImg(){
		return img;
	}
	
	public void setPdfa(boolean pdfa){
		this.pdfa = pdfa;
	}
	
	public boolean getPdfa(){
		return pdfa;
	}

	public long getCod() {
		return codigo;
	}

	public void setCod(int codigo) {
		this.codigo = codigo;
	}
}
