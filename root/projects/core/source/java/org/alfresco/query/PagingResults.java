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
package org.alfresco.query;

import java.util.List;

import org.alfresco.util.Pair;

/**
 * Marker interface for single page of results
 * 
 * @author janv
 * @since 4.0
 */
public interface PagingResults<R>
{
    public List<R> getPage();
    
    /**
     * True if more items on next page. 
     * 
     * Note: could also return true if page was cutoff/trimmed for some reason 
     *       (eg. due to permission checks of large page of requested max items)
     * 
     * @return more items (on next page)
     * 
     *         - true  => at least one more page (ie. at least one more item on the next page)
     *         - false => no more items (ie. this is the last page)
     *         - null  -> unknown whether are more items / pages
     */
    public Boolean hasMoreItems();
    
    /**
     * Get the total result count assuming no paging applied.  This value will only be available if
     * the query supports it and the client requested it.  By default, it is not requested.
     * 
     * Returns result as an approx "range" pair <lower, upper>
     * 
     * - null (or lower is null) => unknown total count (or not requested by the client).
     * - lower = upper           => total count should be accurate
     * - lower < upper           => total count is an approximation ("about") - somewhere in the given range (inclusive)
     * - upper is null           => total count is "more than" lower (upper is unknown)
     * 
     * @return                  Returns the total results as a range (all results, including the paged results returned)
     */
    public Pair<Integer, Integer> getTotalResultCount();
    
    /**
     * Get a unique ID associated with these query results.  This must be available before and
     * after execution i.e. it must depend on the type of query and the query parameters
     * rather than the execution results.  Client has the option to pass this back as a hint when
     * paging.
     * 
     * @return                      a unique ID associated with the query execution results
     */
    public String getQueryExecutionId();
}
