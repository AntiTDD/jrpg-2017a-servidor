package comandos;

import java.io.IOException;

import mensajeria.PaqueteComercio;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ActualizarComercio extends ComandosServer{

	@Override
	public void ejecutar() { //envio el paquete actualizado
		PaqueteComercio paqueteComercio;
		paqueteComercio = (PaqueteComercio) gson.fromJson(cadenaLeida, PaqueteComercio.class);

		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if(conectado.getPaquetePersonaje().getId() == paqueteComercio.getIdComerciante()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(paqueteComercio));
				} catch (IOException e) {
					Servidor.log.append("Falló al intentar enviar paqueteComerciar a:" + conectado.getPaquetePersonaje().getId() + "\n");
				}	
			}
}
		
	}

}
