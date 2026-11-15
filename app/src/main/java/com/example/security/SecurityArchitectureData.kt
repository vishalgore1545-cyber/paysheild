package com.example.security

enum class FirewallDecision {
  ALLOW,
  BLOCK,
}

data class OsiSecurityLayer(
  val layerNumber: Int,
  val layerName: String,
  val primaryRole: String,
  val mechanisms: List<String>,
  val fintechControl: String,
)

data class FirewallTierSpec(
  val tierNumber: Int,
  val name: String,
  val zoneProtected: String,
  val allowedProtocols: String,
  val inspectionRules: List<String>,
  val microservicesGuarded: String,
)

data class FirewallHopTrace(
  val tierNumber: Int,
  val firewallName: String,
  val decision: FirewallDecision,
  val latencyMs: Int,
  val ruleMatched: String,
  val detail: String,
)

data class PacketInspectionResult(
  val payloadPreview: String,
  val sanitizedPayload: String,
  val overallDecision: FirewallDecision,
  val blockedAtLayer: String?,
  val threatCategory: String?,
  val hopTraces: List<FirewallHopTrace>,
)

object SecurityArchitectureCatalog {

  val osiLayers: List<OsiSecurityLayer> = listOf(
    OsiSecurityLayer(
      layerNumber = 1,
      layerName = "Physical Layer",
      primaryRole = "Biometric Data Centers & HSM Vaults",
      mechanisms = listOf(
        "Tier-4 PCI-DSS ISO-27001 Data Centers with mantraps & biometric access",
        "FIPS 140-3 Level 3 Hardware Security Modules (HSM) for UPI PIN & master keys",
        "Tamper-evident fiber conduits & redundant power/air-gap backups",
      ),
      fintechControl = "NPCI UPI keys never leave HSM silicon memory; zero plaintext key export.",
    ),
    OsiSecurityLayer(
      layerNumber = 2,
      layerName = "Data Link Layer",
      primaryRole = "MACsec Encryption & Port Isolation",
      mechanisms = listOf(
        "IEEE 802.1AE MACsec link-layer encryption between rack switches",
        "802.1X port-based network access control & rogue MAC blocking",
        "Private VLAN (PVLAN) isolation preventing lateral ARP spoofing",
      ),
      fintechControl = "Prevents packet sniffing or ARP poisoning inside bank settlement racks.",
    ),
    OsiSecurityLayer(
      layerNumber = 3,
      layerName = "Network Layer",
      primaryRole = "Zero-Trust VPC Micro-Segmentation & IPsec",
      mechanisms = listOf(
        "BGP Anycast DDoS scrubbing at Perimeter Edge",
        "Dedicated VPC subnets per domain (Transfers, Bills, Wealth, Ledger)",
        "IPsec tunnel peering to NPCI UPI Switch & Partner Core Banking APIs",
      ),
      fintechControl = "Ledger DB subnet has zero public route; reachable only via Core Firewall.",
    ),
    OsiSecurityLayer(
      layerNumber = 4,
      layerName = "Transport Layer",
      primaryRole = "TLS 1.3 Pinning & Service-to-Service mTLS",
      mechanisms = listOf(
        "Strict TLS 1.3 (ECDHE-ECDSA-AES256-GCM-SHA384) with HSTS & Cert Pinning",
        "SPIFFE/SPIRE automated mutual TLS (mTLS) certificates across Envoy mesh",
        "SYN-flood protection & adaptive TCP connection rate limiting",
      ),
      fintechControl = "Blocks MITM proxy attacks on mobile devices & rogue internal callers.",
    ),
    OsiSecurityLayer(
      layerNumber = 5,
      layerName = "Session Layer",
      primaryRole = "Device-Bound OAuth2, JWT Rotation & Idempotency",
      mechanisms = listOf(
        "Short-lived RS256/EdDSA JWTs (5 min TTL) + rotating hardware-bound refresh tokens",
        "SIM-binding & Android Keystore hardware attestation per UPI session",
        "Idempotency-Key + cryptographic nonce replay window (60s)",
      ),
      fintechControl = "Prevents session hijacking, token replay, and duplicate debit race conditions.",
    ),
    OsiSecurityLayer(
      layerNumber = 6,
      layerName = "Presentation Layer",
      primaryRole = "AES-256-GCM Payload & Field Tokenization",
      mechanisms = listOf(
        "Envelope encryption (AES-256-GCM) for PII, Aadhaar/PAN & bank account numbers",
        "Strict UTF-8 canonicalization & JSON schema enforcement",
        "Format-preserving tokenization of VPA, card PAN & IFSC before persistence",
      ),
      fintechControl = "Even a raw disk snapshot yields only ciphertext & tokenized references.",
    ),
    OsiSecurityLayer(
      layerNumber = 7,
      layerName = "Application Layer",
      primaryRole = "WAF, SQLi/XSS Sanitization & Behavioral Fraud ML",
      mechanisms = listOf(
        "OWASP CRS Web Application Firewall blocking SQLi, XSS, SSRF & Path Traversal",
        "Parameterized SQL queries exclusively + strict Pydantic/Moshi type schemas",
        "Real-time velocity & anomaly scoring before UPI PIN / NPCI commit",
      ),
      fintechControl = "Zero string-concatenated SQL; every transfer passes multi-layer validation.",
    ),
  )

  val firewallTiers: List<FirewallTierSpec> = listOf(
    FirewallTierSpec(
      tierNumber = 1,
      name = "Perimeter / Edge Firewall",
      zoneProtected = "Public Internet -> CDN & Anycast Scrubbing Edge",
      allowedProtocols = "HTTPS (443/TCP) • QUIC (443/UDP)",
      inspectionRules = listOf(
        "Volumetric L3/L4 DDoS scrubbing & SYN-cookie enforcement",
        "Geo-IP & ASN reputation filtering + Tor/Botnet IP blackhole",
        "Per-IP burst limit: max 120 req/min on auth endpoints",
      ),
      microservicesGuarded = "Global Edge Ingress & DDoS Shield",
    ),
    FirewallTierSpec(
      tierNumber = 2,
      name = "DMZ Firewall",
      zoneProtected = "Edge -> Public API Gateway & Static Web/Mobile Ingress",
      allowedProtocols = "TLS 1.3 (443) -> Internal Envoy Ingress (8443)",
      inspectionRules = listOf(
        "Strict TLS 1.3 termination & mobile certificate pinning verification",
        "Drops all direct RFC1918 / internal service discovery headers",
        "Isolates public frontend & API gateway from backend VPCs",
      ),
      microservicesGuarded = "API Gateway, Auth Edge, QR Resolver",
    ),
    FirewallTierSpec(
      tierNumber = 3,
      name = "Host-Based / Web Application Firewall (WAF)",
      zoneProtected = "API Gateway -> Application Runtime Containers",
      allowedProtocols = "Validated JSON/HTTPS + mTLS + gRPC",
      inspectionRules = listOf(
        "Real-time payload inspection for SQLi (' OR 1=1, UNION SELECT)",
        "XSS & script tag sanitization (<script>, javascript:, onerror=)",
        "JWT signature, device attestation & Idempotency-Key verification",
      ),
      microservicesGuarded = "UPI Transfer Engine, BillPay Biller Hub, Travel Switch",
    ),
    FirewallTierSpec(
      tierNumber = 4,
      name = "Internal / Core Firewall",
      zoneProtected = "Micro-segmented Service Mesh (Transfers <-> Wealth <-> Bills)",
      allowedProtocols = "mTLS gRPC (9090) • Mutual SPIFFE Identity",
      inspectionRules = listOf(
        "Zero-trust East-West micro-segmentation: BillPay cannot query Wealth PII",
        "Least-privilege RBAC claims checked on every inter-service RPC",
        "Egress whitelist restricted strictly to NPCI Switch & BBPS endpoints",
      ),
      microservicesGuarded = "Ledger Core, Fraud Scoring Engine, AutoPay Scheduler, Wealth SIP",
    ),
    FirewallTierSpec(
      tierNumber = 5,
      name = "Database / Data Center Firewall",
      zoneProtected = "Ledger Core -> Encrypted PostgreSQL / Room Vault",
      allowedProtocols = "mTLS PostgreSQL (5432) from Ledger Service CIDR Only",
      inspectionRules = listOf(
        "Denies all ad-hoc DDL, stacked queries, and unparameterized statements",
        "Requires prepared/bound parameters (:id, :amount) for every read/write",
        "Enforces AES-256-GCM column encryption & immutable append-only audit log",
      ),
      microservicesGuarded = "Double-Entry Ledger Store, Vaulted PII & Audit Trail",
    ),
  )
}

object FirewallInspector {

  private val sqliRegex = Regex(
    pattern = "(?i)(\\b(union\\s+select|drop\\s+table|insert\\s+into|delete\\s+from|update\\s+\\w+\\s+set|exec\\s*\\(|xp_cmdshell)\\b|--|;|'\\s*or\\s*'1'='1|'\\s*or\\s*1=1)",
  )
  private val xssRegex = Regex(
    pattern = "(?i)(<\\s*script|</\\s*script|javascript:|onerror\\s*=|onload\\s*=|<\\s*iframe|<\\s*img[^>]+onerror)",
  )
  private val ddosFloodRegex = Regex(
    pattern = "(?i)(flood|ddos|syn_burst|rate_limit_exceeded|botnet_999)",
  )
  private val lateralPivotRegex = Regex(
    pattern = "(?i)(169\\.254\\.169\\.254|ssrf|internal_admin|/etc/passwd|bypass_mtls)",
  )

  fun sanitizeInput(raw: String): String {
    return raw
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("'", "")
      .replace("\"", "")
      .replace(";", "")
      .replace("--", "")
      .trim()
      .take(120)
  }

  fun inspectPayload(
    rawPayload: String,
    sourceIp: String = "103.21.58.42",
    requireStrictVpa: Boolean = false,
  ): PacketInspectionResult {
    val sanitized = sanitizeInput(rawPayload)
    val traces = mutableListOf<FirewallHopTrace>()

    // 1. Perimeter / Edge Firewall
    if (ddosFloodRegex.containsMatchIn(rawPayload) || sourceIp.startsWith("185.220.")) {
      traces += FirewallHopTrace(
        tierNumber = 1,
        firewallName = "Perimeter / Edge Firewall",
        decision = FirewallDecision.BLOCK,
        latencyMs = 1,
        ruleMatched = "EDGE-DDOS-429 / ASN-BLOCK",
        detail = "Blocked anomalous burst / blacklisted Tor exit IP ($sourceIp) at Anycast edge.",
      )
      return PacketInspectionResult(
        payloadPreview = rawPayload,
        sanitizedPayload = sanitized,
        overallDecision = FirewallDecision.BLOCK,
        blockedAtLayer = "Firewall 1: Perimeter / Edge (OSI L3/L4)",
        threatCategory = "Volumetric DDoS / Blacklisted IP",
        hopTraces = traces,
      )
    }
    traces += FirewallHopTrace(
      tierNumber = 1,
      firewallName = "Perimeter / Edge Firewall",
      decision = FirewallDecision.ALLOW,
      latencyMs = 2,
      ruleMatched = "EDGE-GEO-OK",
      detail = "Clean Geo-IP ($sourceIp), SYN rate within 120 req/min budget.",
    )

    // 2. DMZ Firewall
    traces += FirewallHopTrace(
      tierNumber = 2,
      firewallName = "DMZ Firewall",
      decision = FirewallDecision.ALLOW,
      latencyMs = 3,
      ruleMatched = "DMZ-TLS13-PINNED",
      detail = "TLS 1.3 handshake & mobile certificate pinning verified; routed to API Gateway.",
    )

    // 3. Host-Based / WAF
    val isSqli = sqliRegex.containsMatchIn(rawPayload)
    val isXss = xssRegex.containsMatchIn(rawPayload)
    if (isSqli || isXss) {
      val category = if (isSqli) "SQL Injection (SQLi)" else "Cross-Site Scripting (XSS)"
      val rule = if (isSqli) "OWASP-CRS-942100 (SQLi)" else "OWASP-CRS-941100 (XSS)"
      traces += FirewallHopTrace(
        tierNumber = 3,
        firewallName = "Host-Based / WAF",
        decision = FirewallDecision.BLOCK,
        latencyMs = 4,
        ruleMatched = rule,
        detail = "Rejected malicious $category signature in OSI Layer 7 payload before reaching runtime.",
      )
      return PacketInspectionResult(
        payloadPreview = rawPayload,
        sanitizedPayload = sanitized,
        overallDecision = FirewallDecision.BLOCK,
        blockedAtLayer = "Firewall 3: Host-Based WAF (OSI L7)",
        threatCategory = category,
        hopTraces = traces,
      )
    }
    traces += FirewallHopTrace(
      tierNumber = 3,
      firewallName = "Host-Based / WAF",
      decision = FirewallDecision.ALLOW,
      latencyMs = 4,
      ruleMatched = "WAF-OWASP-CLEAN",
      detail = "Passed OWASP SQLi/XSS deep inspection & strict UTF-8 JSON schema check.",
    )

    // 4. Internal / Core Firewall
    if (lateralPivotRegex.containsMatchIn(rawPayload)) {
      traces += FirewallHopTrace(
        tierNumber = 4,
        firewallName = "Internal / Core Firewall",
        decision = FirewallDecision.BLOCK,
        latencyMs = 2,
        ruleMatched = "CORE-MTLS-SEGMENT-DENY",
        detail = "Blocked unauthorized East-West SSRF / metadata pivot across service mesh.",
      )
      return PacketInspectionResult(
        payloadPreview = rawPayload,
        sanitizedPayload = sanitized,
        overallDecision = FirewallDecision.BLOCK,
        blockedAtLayer = "Firewall 4: Internal / Core Mesh (OSI L4/L5)",
        threatCategory = "SSRF / Lateral Privilege Escalation",
        hopTraces = traces,
      )
    }
    traces += FirewallHopTrace(
      tierNumber = 4,
      firewallName = "Internal / Core Firewall",
      decision = FirewallDecision.ALLOW,
      latencyMs = 3,
      ruleMatched = "CORE-SPIFFE-MTLS-OK",
      detail = "mTLS SPIFFE identity verified for Ledger Core microservice.",
    )

    // 5. Database / Data Center Firewall
    if (requireStrictVpa && !rawPayload.contains("@")) {
      traces += FirewallHopTrace(
        tierNumber = 5,
        firewallName = "Database / Data Center Firewall",
        decision = FirewallDecision.BLOCK,
        latencyMs = 2,
        ruleMatched = "DB-SCHEMA-CONSTRAINT",
        detail = "Rejected malformed VPA identifier at parameterized query boundary.",
      )
      return PacketInspectionResult(
        payloadPreview = rawPayload,
        sanitizedPayload = sanitized,
        overallDecision = FirewallDecision.BLOCK,
        blockedAtLayer = "Firewall 5: Database Firewall (OSI L6/L7)",
        threatCategory = "Schema Constraint Violation",
        hopTraces = traces,
      )
    }
    traces += FirewallHopTrace(
      tierNumber = 5,
      firewallName = "Database / Data Center Firewall",
      decision = FirewallDecision.ALLOW,
      latencyMs = 3,
      ruleMatched = "DB-BIND-PARAM-AES256",
      detail = "Executed via Room/PostgreSQL bound parameters (:param) inside AES-256 encrypted store.",
    )

    return PacketInspectionResult(
      payloadPreview = rawPayload,
      sanitizedPayload = sanitized,
      overallDecision = FirewallDecision.ALLOW,
      blockedAtLayer = null,
      threatCategory = null,
      hopTraces = traces,
    )
  }
}
