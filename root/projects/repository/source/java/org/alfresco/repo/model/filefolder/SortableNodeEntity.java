/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.model.filefolder;

import java.util.List;

import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.NodePropertyEntity;

/**
 * Sortable Node Entity - can be optionally sorted by (up to) two sort properties (prop1 followed by prop2)
 * 
 * @author jan
 * @since 4.0
 */
public class SortableNodeEntity
{
    private Long id; // node id
    
    private NodeEntity node;
    private NodePropertyEntity prop1;
    private NodePropertyEntity prop2;
    
    // Supplemental query-related parameters
    private Long parentNodeId;
    private Long prop1qnameId;
    private Long prop2qnameId;
    private List<Long> childNodeTypeQNameIds;
    private boolean auditableProps;
    private boolean nodeType;
    
    /**
     * Default constructor
     */
    public SortableNodeEntity()
    {
        auditableProps = false;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public NodePropertyEntity getProp1()
    {
        return prop1;
    }
    
    public void setProp1(NodePropertyEntity prop1)
    {
        this.prop1 = prop1;
    }
    
    public NodePropertyEntity getProp2()
    {
        return prop2;
    }
    
    public void setProp2(NodePropertyEntity prop2)
    {
        this.prop2 = prop2;
    }
    
    public NodeEntity getNode()
    {
        return node;
    }
    
    public void setNode(NodeEntity childNode)
    {
        this.node = childNode;
    }
    
    // Supplemental query-related parameters
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public void setParentNodeId(Long parentNodeId)
    {
        this.parentNodeId = parentNodeId;
    }
    
    public Long getProp1qnameId()
    {
        return prop1qnameId;
    }
    
    public void setProp1qnameId(Long prop1qnameId)
    {
        this.prop1qnameId = prop1qnameId;
    }
    
    public Long getProp2qnameId()
    {
        return prop2qnameId;
    }
    
    public void setProp2qnameId(Long prop2qnameId)
    {
        this.prop2qnameId = prop2qnameId;
    }
    
    public List<Long> getChildNodeTypeQNameIds()
    {
        return childNodeTypeQNameIds;
    }
    
    public void setChildNodeTypeQNameIds(List<Long> childNodeTypeQNameIds)
    {
        this.childNodeTypeQNameIds = childNodeTypeQNameIds;
    }
    
    public boolean isAuditableProps()
    {
        return auditableProps;
    }
    
    public void setAuditableProps(boolean auditableProps)
    {
        this.auditableProps = auditableProps;
    }
    
    public boolean isNodeType()
    {
        return nodeType;
    }
    
    public void setNodeType(boolean nodeType)
    {
        this.nodeType = nodeType;
    }
}