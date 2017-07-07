package comandos;

import java.io.IOException;

import mensajeria.PaqueteComercio;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Comercio extends ComandosServer{

	@Override
	public void ejecutar() {
		PaqueteComercio paqueteComercio = gson.fromJson(cadenaLeida, PaqueteComercio.class); //casteamos el objeto de gson a un paquetecomercio.
		
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if(conectado.getPaquetePersonaje().getId() == paqueteComercio.getIdComerciante()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(paqueteComercio));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Servidor.log.append("Falló al intentar enviar comercio a:" + conectado.getPaquetePersonaje().getId() + "\n");
				}	
			}
}
		
	}

}
