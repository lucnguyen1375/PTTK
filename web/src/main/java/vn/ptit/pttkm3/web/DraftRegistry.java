package vn.ptit.pttkm3.web;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import vn.ptit.pttkm3.service.ImportDraft;
final class DraftRegistry {
    private static final String KEY=DraftRegistry.class.getName();
    static DraftRegistry get(HttpSession session){synchronized(session){DraftRegistry r=(DraftRegistry)session.getAttribute(KEY);if(r==null){r=new DraftRegistry();session.setAttribute(KEY,r);}return r;}}
    private final Map<String,ImportDraft> drafts=new HashMap<>();
    synchronized void put(ImportDraft d){drafts.put(d.id(),d);}
    synchronized ImportDraft get(String id,long employeeId){ImportDraft d=drafts.get(id);return d!=null&&d.employeeId()==employeeId?d:null;}
    synchronized void remove(String id){drafts.remove(id);}
}
