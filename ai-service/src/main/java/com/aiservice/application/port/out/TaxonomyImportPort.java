package com.aiservice.application.port.out;
import com.aiservice.application.service.TextbookStructureService.Structure;import java.util.UUID;
public interface TaxonomyImportPort{void confirm(UUID subjectId,Structure structure,String bearerToken);}
