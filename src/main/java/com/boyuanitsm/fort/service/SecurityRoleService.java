package com.boyuanitsm.fort.service;

import com.boyuanitsm.fort.domain.SecurityRole;
import com.boyuanitsm.fort.repository.SecurityRoleRepository;
import com.boyuanitsm.fort.repository.search.SecurityRoleSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing SecurityRole.
 */
@Service
@Transactional
public class SecurityRoleService {

    private final Logger log = LoggerFactory.getLogger(SecurityRoleService.class);
    
    @Inject
    private SecurityRoleRepository securityRoleRepository;
    
    @Inject
    private SecurityRoleSearchRepository securityRoleSearchRepository;
    
    /**
     * Save a securityRole.
     * 
     * @param securityRole the entity to save
     * @return the persisted entity
     */
    public SecurityRole save(SecurityRole securityRole) {
        log.debug("Request to save SecurityRole : {}", securityRole);
        SecurityRole result = securityRoleRepository.save(securityRole);
        securityRoleSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the securityRoles.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<SecurityRole> findAll(Pageable pageable) {
        log.debug("Request to get all SecurityRoles");
        Page<SecurityRole> result = securityRoleRepository.findAll(pageable); 
        return result;
    }

    /**
     *  Get one securityRole by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public SecurityRole findOne(Long id) {
        log.debug("Request to get SecurityRole : {}", id);
        SecurityRole securityRole = securityRoleRepository.findOneWithEagerRelationships(id);
        return securityRole;
    }

    /**
     *  Delete the  securityRole by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete SecurityRole : {}", id);
        securityRoleRepository.delete(id);
        securityRoleSearchRepository.delete(id);
    }

    /**
     * Search for the securityRole corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SecurityRole> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of SecurityRoles for query {}", query);
        return securityRoleSearchRepository.search(queryStringQuery(query), pageable);
    }
}