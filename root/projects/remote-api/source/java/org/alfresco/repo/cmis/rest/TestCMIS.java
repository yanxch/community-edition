/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.GUID;
import org.alfresco.web.scripts.Format;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Request;
import org.alfresco.web.scripts.atom.AbderaService;
import org.alfresco.web.scripts.atom.AbderaServiceImpl;
import org.apache.abdera.ext.cmis.CMISConstants;
import org.apache.abdera.ext.cmis.CMISExtensionFactory;
import org.apache.abdera.ext.cmis.CMISProperties;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 * CMIS API Test Harness
 * 
 * @author davidc
 */
public class TestCMIS extends CMISWebScriptTest
{
    private AbderaService abdera;
    
    private static Service service = null;
    private static Entry testRunFolder = null;
    
 
    // TODO: checkout/checkin tests need to perform version property assertions
    
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        AbderaServiceImpl abderaImpl = new AbderaServiceImpl();
        abderaImpl.afterPropertiesSet();
        abderaImpl.registerExtensionFactory(new CMISExtensionFactory());
        abdera = abderaImpl;
    }

    private Service getRepository()
        throws Exception
    {
        if (service == null)
        {
            MockHttpServletResponse res = sendRequest(new GetRequest("/api/repository"), 200);
            String xml = res.getContentAsString();
            assertNotNull(xml);
            assertTrue(xml.length() > 0);
            //assertValidXML(xml, getCMISValidator().getAppValidator());
            
            service = abdera.parseService(new StringReader(xml), null);
            assertNotNull(service);
        }
        return service;
    }
    
    private IRI getRootCollection(Service service)
    {
        Collection root = service.getCollection("Main Repository", "root collection");
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    private IRI getCheckedOutCollection(Service service)
    {
        Collection root = service.getCollection("Main Repository", "checkedout collection");
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    private Entry createTestFolder(String name)
        throws Exception
    {
        if (testRunFolder == null)
        {
            Service service = getRepository();
            IRI rootFolderHREF = getRootCollection(service);
            testRunFolder = createFolder(rootFolderHREF, "CMIS Test Run " + System.currentTimeMillis());
        }
        Link childrenLink = testRunFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry testFolder = createFolder(childrenLink.getHref(), name + " " + System.currentTimeMillis());
        return testFolder;
    }

    private Entry getEntry(IRI href)
        throws Exception
    {
        return getEntry(href, null);
    }

    private Entry getEntry(IRI href, Map<String, String> args)
        throws Exception
    {
        Request get = new GetRequest(href.toString()).setArgs(args);
        MockHttpServletResponse res = sendRequest(get, 200);
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals(getArgsAsHeaders() ? get.getUri() : get.getFullUri(), entry.getSelfLink().getHref().toString());
        return entry;
    }

    private Feed getFeed(IRI href)
        throws Exception
    {
        return getFeed(href, null);
    }
    
    private Feed getFeed(IRI href, Map<String, String> args)
        throws Exception
    {
        Request get = new GetRequest(href.toString()).setArgs(args);
        MockHttpServletResponse res = sendRequest(get, 200);
        assertNotNull(res);
        String xml = res.getContentAsString();
        Feed feed = abdera.parseFeed(new StringReader(xml), null);
        assertNotNull(feed);
        assertEquals(getArgsAsHeaders() ? get.getUri() : get.getFullUri(), feed.getSelfLink().getHref().toString());
        return feed;
    }
    
    private Entry createFolder(IRI parent, String name)
        throws Exception
    {
        String createFolder = loadString("/cmis/rest/createfolder.atomentry.xml");
        String guid = GUID.generate();
        createFolder = createFolder.replace("${NAME}", name);
        createFolder = createFolder.replace("${GUID}", guid);
        MockHttpServletResponse res = sendRequest(new PostRequest(parent.toString(), createFolder, Format.ATOMENTRY.mimetype()), 201);
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals("Title " + name + " " + guid, entry.getTitle());
        assertEquals("Summary " + name + " " + guid, entry.getSummary());
        CMISProperties props = entry.getExtension(CMISConstants.PROPERTIES);
        assertEquals("folder", props.getBaseType());
        String testFolderHREF = (String)res.getHeader("Location");
        assertNotNull(testFolderHREF);
        return entry;
    }

    private Entry createDocument(IRI parent, String name)
        throws Exception
    {
        String createFile = loadString("/cmis/rest/createdocument.atomentry.xml");
        String guid = GUID.generate();
        createFile = createFile.replace("${NAME}", name);
        createFile = createFile.replace("${GUID}", guid);
        MockHttpServletResponse res = sendRequest(new PostRequest(parent.toString(), createFile, Format.ATOMENTRY.mimetype()), 201);
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals("Title " + name + " " + guid, entry.getTitle());
        assertEquals("Summary " + name + " " + guid, entry.getSummary());
        assertNotNull(entry.getContentSrc());
        CMISProperties props = entry.getExtension(CMISConstants.PROPERTIES);
        assertEquals("document", props.getBaseType());
        String testFileHREF = (String)res.getHeader("Location");
        assertNotNull(testFileHREF);
        return entry;
    }

    public void testRepository()
        throws Exception
    {
        Service service = getRepository();
        IRI rootHREF = getRootCollection(service);
        sendRequest(new GetRequest(rootHREF.toString()), 200);
    }
    
    public void testCreateDocument()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateDocument");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry document = createDocument(children.getSelfLink().getHref(), "testCreateDocument");
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
        assertNotNull(entry);
    }
    
    public void testCreateFolder()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateFolder");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry folder = createFolder(children.getSelfLink().getHref(), "testCreateFolder");
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(folder.getId().toString());
        assertNotNull(entry);
    }
    
    public void testGet()
        throws Exception
    {
        // get folder
        Entry testFolder = createTestFolder("testGet");
        assertNotNull(testFolder);
        Entry testFolderFromGet = getEntry(testFolder.getSelfLink().getHref());
        assertEquals(testFolder.getId(), testFolderFromGet.getId());
        assertEquals(testFolder.getTitle(), testFolderFromGet.getTitle());
        assertEquals(testFolder.getSummary(), testFolderFromGet.getSummary());
        
        // get document
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry testDocument = createDocument(childrenLink.getHref(), "testGet");
        assertNotNull(testDocument);
        Entry testDocumentFromGet = getEntry(testDocument.getSelfLink().getHref());
        assertEquals(testDocument.getId(), testDocumentFromGet.getId());
        assertEquals(testDocument.getTitle(), testDocumentFromGet.getTitle());
        assertEquals(testDocument.getSummary(), testDocumentFromGet.getSummary());
        
        // get something that doesn't exist
        MockHttpServletResponse res = sendRequest(new GetRequest("/api/node/workspace/SpacesStore/" + GUID.generate()), 404);
        assertNotNull(res);
    }

    public void testChildren()
        throws Exception
    {
        // create multiple children
        Entry testFolder = createTestFolder("testChildren");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testChildren1");
        assertNotNull(document1);
        Entry document2 = createDocument(childrenLink.getHref(), "testChildren2");
        assertNotNull(document2);
        Entry document3 = createDocument(childrenLink.getHref(), "testChildren3");
        assertNotNull(document3);
        
        // checkout one of the children to ensure private working copy isn't included
        MockHttpServletResponse documentRes = sendRequest(new GetRequest(document2.getSelfLink().getHref().toString()), 200);
        assertNotNull(documentRes);
        String documentXML = documentRes.getContentAsString();
        assertNotNull(documentXML);
        IRI checkedoutHREF = getCheckedOutCollection(service);
        MockHttpServletResponse pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), documentXML, Format.ATOMENTRY.mimetype()), 201);
        assertNotNull(pwcRes);
        Entry pwc = abdera.parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        
        // get children, ensure they exist (but not private working copy)
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        assertEquals(3, children.getEntries().size());
        assertNotNull(children.getEntry(document1.getId().toString()));
        assertNotNull(children.getEntry(document2.getId().toString()));
        assertNotNull(children.getEntry(document3.getId().toString()));
        assertNull(children.getEntry(pwc.getId().toString()));
        
        // TODO: paging
    }
    
    public void testGetParent()
        throws Exception
    {
        Entry testFolder = createTestFolder("testParent");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry childFolder = createFolder(childrenLink.getHref(), "testParentChild");
        assertNotNull(childFolder);
        Link parentLink = childFolder.getLink(CMISConstants.REL_PARENT);
        assertNotNull(parentLink);

        // ensure there is parent 'testParent'
        Feed parent = getFeed(parentLink.getHref());
        assertNotNull(parent);
        assertEquals(1, parent.getEntries().size());
        assertEquals(testFolder.getId(), parent.getEntries().get(0).getId());

        // ensure there are ancestors 'testParent', "test run folder" and "root folder"
        Map<String, String> args = new HashMap<String, String>();
        args.put("returnToRoot", "true");
        Feed parentsToRoot = getFeed(new IRI(parentLink.getHref().toString()), args);
        assertNotNull(parentsToRoot);
        assertEquals(3, parentsToRoot.getEntries().size());
        assertEquals(testFolder.getId(), parentsToRoot.getEntries().get(0).getId());
        assertEquals(testRunFolder.getId(), parentsToRoot.getEntries().get(1).getId());
        Feed root = getFeed(getRootCollection(getRepository()));
        assertEquals(root.getId(), parentsToRoot.getEntries().get(2).getId());
    }

    public void testGetParents()
        throws Exception
    {
        Entry testFolder = createTestFolder("testParents");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry childDocs = createDocument(childrenLink.getHref(), "testParentsChild");
        assertNotNull(childDocs);
        Link parentLink = childDocs.getLink(CMISConstants.REL_PARENTS);
        assertNotNull(parentLink);
        
        // ensure there is parent 'testParent'
        Feed parent = getFeed(parentLink.getHref());
        assertNotNull(parent);
        assertEquals(1, parent.getEntries().size());
        assertEquals(testFolder.getId(), parent.getEntries().get(0).getId());

        // ensure there are ancestors 'testParent', "test run folder" and "root folder"
        Map<String, String> args = new HashMap<String, String>();
        args.put("returnToRoot", "true");
        Feed parentsToRoot = getFeed(new IRI(parentLink.getHref().toString()), args);
        assertNotNull(parentsToRoot);
        assertEquals(3, parentsToRoot.getEntries().size());
        assertEquals(testFolder.getId(), parentsToRoot.getEntries().get(0).getId());
        assertEquals(testRunFolder.getId(), parentsToRoot.getEntries().get(1).getId());
        Feed root = getFeed(getRootCollection(getRepository()));
        assertEquals(root.getId(), parentsToRoot.getEntries().get(2).getId());
    }
    
    public void testDelete()
        throws Exception
    {
        // retrieve test folder for deletes
        Entry testFolder = createTestFolder("testDelete");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        Feed children = getFeed(childrenLink.getHref());
        int entriesBefore = children.getEntries().size();
        
        // create document for delete
        Entry document = createDocument(childrenLink.getHref(), "testDelete");
        MockHttpServletResponse documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200);
        assertNotNull(documentRes);

        // ensure document has been created
        Feed children2 = getFeed(childrenLink.getHref());
        assertNotNull(children2);
        int entriesAfterCreate = children2.getEntries().size();
        assertEquals(entriesAfterCreate, entriesBefore +1);

        // delete
        MockHttpServletResponse deleteRes = sendRequest(new DeleteRequest(document.getSelfLink().getHref().toString()), 204);
        assertNotNull(deleteRes);

        // ensure document has been deleted
        Feed children3 = getFeed(childrenLink.getHref());
        assertNotNull(children3);
        int entriesAfterDelete = children3.getEntries().size();
        assertEquals(entriesBefore, entriesAfterDelete);
    }

    public void testUpdate()
        throws Exception
    {
        // retrieve test folder for update
        Entry testFolder = createTestFolder("testUpdate");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document for update
        Entry document = createDocument(childrenLink.getHref(), "testUpdate");
        assertNotNull(document);
        assertEquals("text/html", document.getContentMimeType().toString());

        // update
        String updateFile = loadString("/cmis/rest/updatedocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${GUID}", guid);
        MockHttpServletResponse res = sendRequest(new PutRequest(document.getSelfLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200);
        assertNotNull(res);
        Entry updated = abdera.parseEntry(new StringReader(res.getContentAsString()), null);
        
        // ensure update occurred
        assertEquals(document.getId(), updated.getId());
        assertEquals(document.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        assertEquals("text/plain", updated.getContentMimeType().toString());
        MockHttpServletResponse contentRes = sendRequest(new GetRequest(updated.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, contentRes.getContentAsString());
    }

    public void testGetCheckedOut()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testGetCheckedOut");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        Feed scope = getFeed(childrenLink.getHref());
        assertNotNull(scope);
        CMISProperties props = scope.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        assertNotNull(scopeId);
        
        // retrieve checkouts within scope of test checkout folder
        Service repository = getRepository();
        assertNotNull(repository);
        IRI checkedoutHREF = getCheckedOutCollection(service);
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(0, checkedout.getEntries().size());
    }
    
    public void testCheckout()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testCheckout");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        Feed scope = getFeed(childrenLink.getHref());
        
        // create document for checkout
        Entry document = createDocument(scope.getSelfLink().getHref(), "testCheckout");
        MockHttpServletResponse documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200);
        assertNotNull(documentRes);
        String documentXML = documentRes.getContentAsString();
        assertNotNull(documentXML);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(service);
        MockHttpServletResponse pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), documentXML, Format.ATOMENTRY.mimetype()), 201);
        assertNotNull(pwcRes);
        // TODO: test private working copy properties

        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
    }
    
    public void testCancelCheckout()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testCancelCheckout");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        Feed scope = getFeed(childrenLink.getHref());
        
        // create document for checkout
        Entry document = createDocument(scope.getSelfLink().getHref(), "testCancelCheckout");
        MockHttpServletResponse documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200);
        assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        assertNotNull(xml);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(service);
        MockHttpServletResponse pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201);
        assertNotNull(pwcRes);
        
        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
        
        // cancel checkout
        String pwcXml = pwcRes.getContentAsString();
        Entry pwc = abdera.parseEntry(new StringReader(pwcXml), null);
        assertNotNull(pwc);
        MockHttpServletResponse cancelRes = sendRequest(new DeleteRequest(pwc.getSelfLink().getHref().toString()), 204);
        assertNotNull(cancelRes);

        // test getCheckedOut is updated
        CMISProperties props2 = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId2 = props2.getObjectId();
        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("folderId", scopeId2);
        Feed checkedout2 = getFeed(new IRI(checkedoutHREF.toString()), args2);
        assertNotNull(checkedout2);
        assertEquals(0, checkedout2.getEntries().size());
    }

    public void testCheckIn()
        throws Exception
    {
        // retrieve test folder for checkins
        Entry testFolder = createTestFolder("testCheckIn");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        Feed scope = getFeed(childrenLink.getHref());
        
        // create document for checkout
        Entry document = createDocument(scope.getSelfLink().getHref(), "testCheckin");
        MockHttpServletResponse documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200);
        assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        assertNotNull(xml);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(service);
        MockHttpServletResponse pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201);
        assertNotNull(pwcRes);
        Entry pwc = abdera.parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        assertNotNull(pwc);
        
        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());

        // test update of private working copy
        String updateFile = loadString("/cmis/rest/updatedocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${GUID}", guid);
        MockHttpServletResponse pwcUpdatedres = sendRequest(new PutRequest(pwc.getEditLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200);
        assertNotNull(pwcUpdatedres);
        Entry updated = abdera.parseEntry(new StringReader(pwcUpdatedres.getContentAsString()), null);
        // ensure update occurred
        assertEquals(pwc.getId(), updated.getId());
        assertEquals(pwc.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        assertEquals("text/plain", updated.getContentMimeType().toString());
        MockHttpServletResponse pwcContentRes = sendRequest(new GetRequest(pwc.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, pwcContentRes.getContentAsString());
        
        // checkin
        String checkinFile = loadString("/cmis/rest/checkindocument.atomentry.xml");
        String checkinUrl = pwc.getSelfLink().getHref().toString();
        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("checkinComment", guid);
        MockHttpServletResponse checkinRes = sendRequest(new PutRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200);
        assertNotNull(checkinRes);
    
        // test getCheckedOut is updated
        CMISProperties props2 = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId2 = props2.getObjectId();
        Map<String, String> args3 = new HashMap<String, String>();
        args3.put("folderId", scopeId2);
        Feed checkedout2 = getFeed(new IRI(checkedoutHREF.toString()), args3);
        assertNotNull(checkedout2);
        assertEquals(0, checkedout2.getEntries().size());
        
        // test checked-in doc has new updates
        Entry checkedIn = abdera.parseEntry(new StringReader(checkinRes.getContentAsString()), null);
        Entry updatedDoc = getEntry(checkedIn.getSelfLink().getHref());
        // TODO: issue with updating name on PWC and it not reflecting on checked-in document
        //assertEquals("Updated Title " + guid, updatedDoc.getTitle());
        assertEquals("text/plain", updatedDoc.getContentMimeType().toString());
        MockHttpServletResponse updatedContentRes = sendRequest(new GetRequest(updatedDoc.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, updatedContentRes.getContentAsString());
    }

//    public void testUnfiled()
//    {
//    }
    
}
