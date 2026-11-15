package com.example

import com.example.security.FirewallDecision
import com.example.security.FirewallInspector
import com.example.security.SecurityArchitectureCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityInspectorTest {

  @Test
  fun `clean UPI payload passes all 5 firewalls`() {
    val result = FirewallInspector.inspectPayload("pay:freshmart@okmerchant amount=250.00")
    assertEquals(FirewallDecision.ALLOW, result.overallDecision)
    assertEquals(5, result.hopTraces.size)
  }

  @Test
  fun `SQL injection payload is blocked at Host WAF tier`() {
    val result = FirewallInspector.inspectPayload("vpa=' OR '1'='1'; DROP TABLE ledger;--")
    assertEquals(FirewallDecision.BLOCK, result.overallDecision)
    assertTrue(result.blockedAtLayer?.contains("WAF") == true)
  }

  @Test
  fun `XSS script tag payload is blocked at Host WAF tier`() {
    val result = FirewallInspector.inspectPayload("<script>alert(1)</script>")
    assertEquals(FirewallDecision.BLOCK, result.overallDecision)
    assertEquals("Cross-Site Scripting (XSS)", result.threatCategory)
  }

  @Test
  fun `DDoS burst is blocked at Perimeter Edge tier`() {
    val result = FirewallInspector.inspectPayload("syn_burst_flood", sourceIp = "185.220.101.4")
    assertEquals(FirewallDecision.BLOCK, result.overallDecision)
    assertEquals(1, result.hopTraces.first().tierNumber)
  }

  @Test
  fun `OSI model and firewall catalog contain 7 layers and 5 tiers`() {
    assertEquals(7, SecurityArchitectureCatalog.osiLayers.size)
    assertEquals(5, SecurityArchitectureCatalog.firewallTiers.size)
  }
}
