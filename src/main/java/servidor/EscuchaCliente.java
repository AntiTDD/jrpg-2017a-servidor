package servidor;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;

import cliente.*;
import dominio.*;
import estados.Estado;
import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteAtacar;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeMovimientos;
import mensajeria.PaqueteDePersonajes;
import mensajeria.PaqueteFinalizarBatalla;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;
import servidor.Conector;
import comandos.ComandosServer;

public class EscuchaCliente extends Thread {

	private final Socket socket;
	private final ObjectInputStream entrada;
	private final ObjectOutputStream salida;
	private int idPersonaje;
	private final Gson gson = new Gson();
	
	private PaquetePersonaje paquetePersonaje;
	private PaqueteMovimiento paqueteMovimiento;
	private PaqueteBatalla paqueteBatalla;
	private PaqueteAtacar paqueteAtacar;
	private PaqueteFinalizarBatalla paqueteFinalizarBatalla;
	
	private PaqueteDeMovimientos paqueteDeMovimiento;
	private PaqueteDePersonajes paqueteDePersonajes;
	private PaqueteUsuario paqueteUsuario;
	
	public EscuchaCliente(String ip, Socket socket, ObjectInputStream entrada, ObjectOutputStream salida) {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
		paquetePersonaje = new PaquetePersonaje();
	}

	public void run() {
		try {
			
			ComandosServer comand;
			Paquete paquete;
			Paquete paqueteSv = new Paquete(null, 0);
			paqueteUsuario = new PaqueteUsuario();

			String cadenaLeida = (String) entrada.readObject();
		
			while (!((paquete = gson.fromJson(cadenaLeida, Paquete.class)).getComando() == Comando.DESCONECTAR)){
				comand = (ComandosServer) paquete.getObjeto("comandos");
				comand.setCadena(cadenaLeida);
				comand.setEscuchaCliente(this);
				comand.ejecutar();
				cadenaLeida = (String) entrada.readObject();
			}
			
			entrada.close();
			salida.close();
			socket.close();
			
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				paqueteDePersonajes = new PaqueteDePersonajes(Servidor.getPersonajesConectados());
				paqueteDePersonajes.setComando(Comando.CONEXION);
				conectado.salida.writeObject(gson.toJson(paqueteDePersonajes, PaqueteDePersonajes.class));
			}

			Servidor.log.append(paquete.getIp() + " se ha desconectado." + System.lineSeparator());

		} catch (IOException | ClassNotFoundException e) {
			Servidor.log.append("Error de conexion: " + e.getMessage() + System.lineSeparator());
			e.printStackTrace();
		} 
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public ObjectInputStream getEntrada() {
		return entrada;
	}
	
	public ObjectOutputStream getSalida() {
		return salida;
	}
	
	public PaquetePersonaje getPaquetePersonaje(){
		return paquetePersonaje;
	}
	
	public int getIdPersonaje() {
		return idPersonaje;
	}

	public PaqueteMovimiento getPaqueteMovimiento() {
		 return paqueteMovimiento;
	}
		 
	public void setPaqueteMovimiento(PaqueteMovimiento paqueteMovimiento) {
		this.paqueteMovimiento = paqueteMovimiento;
	}
		 
	public PaqueteBatalla getPaqueteBatalla() {
		return paqueteBatalla;
	}
		 
	public void setPaqueteBatalla(PaqueteBatalla paqueteBatalla) {
		this.paqueteBatalla = paqueteBatalla;
	}
		 
	public PaqueteAtacar getPaqueteAtacar() {
		return paqueteAtacar;
	}
		 
	public void setPaqueteAtacar(PaqueteAtacar paqueteAtacar) {
		this.paqueteAtacar = paqueteAtacar;
	}
		 
	public PaqueteFinalizarBatalla getPaqueteFinalizarBatalla() {
		return paqueteFinalizarBatalla;
	}
		 
	public void setPaqueteFinalizarBatalla(PaqueteFinalizarBatalla paqueteFinalizarBatalla) {
		this.paqueteFinalizarBatalla = paqueteFinalizarBatalla;
	}
		 
	public PaqueteDeMovimientos getPaqueteDeMovimiento() {
		return paqueteDeMovimiento;
	}
		 
	public void setPaqueteDeMovimiento(PaqueteDeMovimientos paqueteDeMovimiento) {
		this.paqueteDeMovimiento = paqueteDeMovimiento;
	}
		 
	public PaqueteDePersonajes getPaqueteDePersonajes() {
		return paqueteDePersonajes;
	}
		 
	public void setPaqueteDePersonajes(PaqueteDePersonajes paqueteDePersonajes) {
		this.paqueteDePersonajes = paqueteDePersonajes;
	}
		
	public void setIdPersonaje(int idPersonaje) {
		this.idPersonaje = idPersonaje;
	}
		 
	public void setPaquetePersonaje(PaquetePersonaje paquetePersonaje) {
		if(paquetePersonaje.getGanoBatalla()==true) {
			paquetePersonaje.aniadirItem(Conector.darItemRand());
		}
		this.paquetePersonaje = paquetePersonaje;
	}
		 
	public PaqueteUsuario getPaqueteUsuario() {
		return paqueteUsuario;
	}
		 
	public void setPaqueteUsuario(PaqueteUsuario paqueteUsuario) {
		this.paqueteUsuario = paqueteUsuario;
	}
	
	
}

