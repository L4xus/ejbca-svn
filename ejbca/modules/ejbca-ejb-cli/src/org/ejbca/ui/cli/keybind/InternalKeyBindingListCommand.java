/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ui.cli.keybind;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.certificates.certificate.CertificateInfo;
import org.cesecore.certificates.certificate.CertificateStoreSessionRemote;
import org.cesecore.keys.token.CryptoTokenManagementSessionRemote;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.ejbca.core.ejb.signer.InternalKeyBinding;
import org.ejbca.core.ejb.signer.InternalKeyBindingMgmtSessionRemote;

/**
 * List InternalKeyBindings.
 * 
 * @version $Id$
 */
public class InternalKeyBindingListCommand extends BaseInternalKeyBindingCommand {

    @Override
    public String getSubCommand() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all available InternalKeyBindings";
    }

    @Override
    public void executeCommand(Integer internalKeyBindingId, String[] args) throws AuthorizationDeniedException, CryptoTokenOfflineException, Exception {
        final InternalKeyBindingMgmtSessionRemote internalKeyBindingMgmtSession = ejb.getRemoteSession(InternalKeyBindingMgmtSessionRemote.class);
        final CryptoTokenManagementSessionRemote cryptoTokenManagementSession = ejb.getRemoteSession(CryptoTokenManagementSessionRemote.class);
        final CertificateStoreSessionRemote certificateStoreSession = ejb.getRemoteSession(CertificateStoreSessionRemote.class);
        final List<Integer> ids = internalKeyBindingMgmtSession.getInternalKeyBindingIds(getAdmin(), null);
        final List<InternalKeyBinding> internalKeyBindings = new LinkedList<InternalKeyBinding>();
        for (Integer id : ids) {
            internalKeyBindings.add(internalKeyBindingMgmtSession.getInternalKeyBinding(getAdmin(), id.intValue()));
        }
        // Sort by type and name
        Collections.sort(internalKeyBindings, new Comparator<InternalKeyBinding>(){
            @Override
            public int compare(InternalKeyBinding o1, InternalKeyBinding o2) {
                final int typeCompare = o1.getImplementationAlias().compareTo(o1.getImplementationAlias());
                if (typeCompare != 0) {
                    return typeCompare;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        getLogger().info(" Type\t\"Name\" (id), Status, IssuerDN, SerialNumber, \"CryptoTokenName\" (id), KeyPairAlias, {Implementations specific properties}");
        for (final InternalKeyBinding internalKeyBinding : internalKeyBindings) {
            final StringBuilder sb = new StringBuilder();
            sb.append(' ').append(internalKeyBinding.getImplementationAlias());
            sb.append('\t').append('\"').append(internalKeyBinding.getName()).append('\"');
            sb.append(" (").append(internalKeyBinding.getId()).append(')');            
            sb.append(", ").append(internalKeyBinding.getStatus().name());
            final CertificateInfo certificateInfo = certificateStoreSession.getCertificateInfo(internalKeyBinding.getCertificateId());
            sb.append(", ").append(certificateInfo.getIssuerDN()).append(" ").append(certificateInfo.getSerialNumber().toString(16).toUpperCase());
            final int cryptoTokenId = internalKeyBinding.getCryptoTokenId();
            final String cryptoTokenName = cryptoTokenManagementSession.getCryptoTokenInfo(getAdmin(), cryptoTokenId).getName();
            sb.append(", \"").append(cryptoTokenName).append("\" (").append(cryptoTokenId).append(')');
            sb.append(", ").append(internalKeyBinding.getKeyPairAlias());
            sb.append(", {");
            final Set<Entry<Object,Object>> entrySet = internalKeyBinding.getDataMapToPersist().entrySet();
            for (final Entry<Object,Object> entry : entrySet) {
                sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
            }
            if (entrySet.size() > 0) {
                sb.deleteCharAt(sb.length()-1);
            }
            sb.append("}");
            getLogger().info(sb);
        }
        if (internalKeyBindings.size()==0) {
            getLogger().info(" No InternalKeyBindings available or you are not authorized to view any.");
        }
    }
}
