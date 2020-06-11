package com.pepenacho.springboot.backend.apirest.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pepenacho.springboot.backend.apirest.models.entity.Cliente;
import com.pepenacho.springboot.backend.apirest.models.services.IClienteService;

//damos acceso para que pueda enviar/recibir datos, en este caso a angular
@CrossOrigin(origins = { "http://localhost:4200" })

@RestController
@RequestMapping("/api")
public class ClienteRestController {

	@Autowired
	private IClienteService clienteService;

	@GetMapping("/clientes")
	public List<Cliente> index() {
		return clienteService.findAll();

	}

	// ResponseEntity<?> para manejar error al no encontrar un id de cliente
	@GetMapping("/clientes/{id}")
	public ResponseEntity<?> show(@PathVariable(value = "id") Long id) {

		Cliente cliente = null;

		Map<String, Object> response = new HashMap<>();

		try {

			cliente = clienteService.findById(id);

		} catch (DataAccessException e) {
			response.put("mensaje", "Error al realizar la consulta en la BD!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (cliente == null) {
			response.put("mensaje", "El cliente con ID: ".concat(id.toString().concat(" no existe en la BD!")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);
	}

	// como la solicitud viene en formato json, se utiliza @RequestBody
	@PostMapping("/clientes")
	// si se crea correctamente respondera con un status 201
	// @ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente, BindingResult result) {

		Cliente clienteNew = null;

		Map<String, Object> response = new HashMap<>();
		
		if(result.hasErrors()) {
			
			/*List<String> errors = new ArrayList<>();
			
			for(FieldError err: result.getFieldErrors()) {
				errors.add("El campo '".concat(err.getField()).concat("' ").concat(err.getDefaultMessage()));
			}*/
			
			//forma con jdk8 o superior
			List<String> errors = result.getFieldErrors()
					.stream()
					.map(err -> {
						return "El campo '".concat(err.getField()).concat("' ").concat(err.getDefaultMessage());
					})
					.collect(Collectors.toList()); //esta linea convierne un FieldError a una lista
			
			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);//error 400
		}

		try {
			clienteNew = clienteService.save(cliente);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al realizar Insert en la BD!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("mensaje", "El cliente ha sido creado con éxito!");
		response.put("cliente", clienteNew);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@PutMapping("/clientes/{id}")
	// @ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable(value = "id") Long id) {

		Cliente clienteActual = clienteService.findById(id);
		Cliente clienteUpdate = null;

		Map<String, Object> response = new HashMap<>();
		
			if(result.hasErrors()) {
				//forma con jdk8 o superior
				List<String> errors = result.getFieldErrors()
						.stream()
						.map(err -> {
							return "El campo '".concat(err.getField()).concat("' ").concat(err.getDefaultMessage());
						})
						.collect(Collectors.toList()); //esta linea convierne un FieldError a una lista
				
				response.put("errors", errors);
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);//error 400
		}

		if (clienteActual == null) {
			response.put("mensaje", "Error: No se pudo editar. El cliente con ID: "
					.concat(id.toString().concat(" no existe en la BD!")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		try {

			clienteActual.setNombre(cliente.getNombre());
			clienteActual.setApellido(cliente.getApellido());
			clienteActual.setEmail(cliente.getEmail());

			clienteUpdate = clienteService.save(clienteActual);

		} catch (DataAccessException e) {
			response.put("mensaje", "Error al actualizar el cliente en la BD!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido actualizado con éxito!");
		response.put("cliente", clienteUpdate);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);

	}

	@DeleteMapping("clientes/{id}")
	// @ResponseStatus(code = HttpStatus.NO_CONTENT) // status 204 si esta ok
	public ResponseEntity<?> delete(@PathVariable(value = "id") Long id) {

		Map<String, Object> response = new HashMap<>();
		try {
			clienteService.delete(id);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al eliminar el cliente en la BD!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		response.put("mensaje", "El cliente ha sido eliminado con éxito!");
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		
	}

}
