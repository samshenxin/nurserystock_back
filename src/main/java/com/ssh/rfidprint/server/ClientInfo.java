package com.ssh.rfidprint.server;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * GcClientInfo entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "gc_client_info")
public class ClientInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    // Fields
    private Integer           id;
    private String            deviceId;
    private String            host;
    private String            branchId;
    private Integer           status;
    private Timestamp         modifyTime;
    private String            printRibbon;
    private String            indentRibbon;
    private String            topperRibbon;

    // Constructors
    /** default constructor */
    public ClientInfo() {
    }

    // Property accessors
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "device_id", length = 100)
    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Column(name = "host", length = 30)
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Column(name = "branch_id", length = 100)
    public String getBranchId() {
        return this.branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    @Column(name = "status")
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "modify_time", length = 19)
    public Timestamp getModifyTime() {
        return this.modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = result + ((deviceId == null) ? 0 : deviceId.hashCode());
        return result;
    }

    @Column(name = "print_ribbon")
    public String getPrintRibbon() {
        return printRibbon;
    }

    public void setPrintRibbon(String printRibbon) {
        this.printRibbon = printRibbon;
    }

    @Column(name = "indent_ribbon")
    public String getIndentRibbon() {
        return indentRibbon;
    }

    public void setIndentRibbon(String indentRibbon) {
        this.indentRibbon = indentRibbon;
    }
    
    @Column(name = "topper_ribbon")
    public String getTopperRibbon() {
        return topperRibbon;
    }

    public void setTopperRibbon(String topperRibbon) {
        this.topperRibbon = topperRibbon;
    }
}