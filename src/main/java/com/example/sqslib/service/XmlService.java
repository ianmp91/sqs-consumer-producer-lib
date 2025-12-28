package com.example.sqslib.service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ian.paris
 * @since 2025-12-19
 */
@Service
public class XmlService {
    // Cache para no recrear el contexto cada vez (Mejora de rendimiento crítica)
    private final Map<Class<?>, JAXBContext> contextCache = new ConcurrentHashMap<>();

    private final JAXBContext context;

    public XmlService() throws JAXBException {
        this.context = JAXBContext.newInstance("com.example.sqslib.iata");
    }

    /**
     * Convierte cualquier objeto del paquete IATA a XML.
     */
    public String toXml(Object object) {
        try {
            Marshaller marshaller = context.createMarshaller();

            // Configuración para producción (compacto) vs debug
            // Sugerencia: En PROD usar FALSE para ahorrar costos de transferencia en SQS
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // Sin <?xml ...?>

            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Error serializando objeto IATA a XML: " + object.getClass().getSimpleName(), e);
        }
    }

    /**
     * MÉTODO CLAVE PARA MICROSERVICIO (C).
     * Deserializa sin saber la clase de antemano. JAXB descubre el tipo por el root element.
     */
    public Object fromXml(String xml) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xml);

            // JAXB lee el tag raíz (ej: <IATA_AIDX_FlightLegRQ>) y busca en el ObjectFactory
            // qué clase corresponde.
            return unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException("Error deserializando XML genérico", e);
        }
    }

    /**
     * Método opcional si sabes exactamente qué esperas (útil para Microservicio B leyendo respuestas).
     * Realiza un cast seguro.
     */
    public <T> T fromXml(String xml, Class<T> expectedType) {
        Object result = fromXml(xml);
        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        } else {
            throw new ClassCastException("Se esperaba " + expectedType.getSimpleName() +
                    " pero el XML contenía " + result.getClass().getSimpleName());
        }
    }
}
