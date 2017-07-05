package comandos;

import mensajeria.PaqueteMovimiento;
import servidor.Servidor;

public class Movimiento extends ComandosServer {

	@Override
	public void ejecutar() {
		escuchaCliente.setPaqueteMovimiento((PaqueteMovimiento) (gson.fromJson((String) cadenaLeida, PaqueteMovimiento.class)));
		
		Servidor.getUbicacionPersonajes().get(escuchaCliente.getPaqueteMovimiento().getIdPersonaje()).setPosX(escuchaCliente.getPaqueteMovimiento().getPosX());
		Servidor.getUbicacionPersonajes().get(escuchaCliente.getPaqueteMovimiento().getIdPersonaje()).setPosY(escuchaCliente.getPaqueteMovimiento().getPosY());
		Servidor.getUbicacionPersonajes().get(escuchaCliente.getPaqueteMovimiento().getIdPersonaje()).setDireccion(escuchaCliente.getPaqueteMovimiento().getDireccion());
		Servidor.getUbicacionPersonajes().get(escuchaCliente.getPaqueteMovimiento().getIdPersonaje()).setFrame(escuchaCliente.getPaqueteMovimiento().getFrame());
		
		synchronized(Servidor.atencionMovimientos){
			Servidor.atencionMovimientos.notify();
		}

	}

}
