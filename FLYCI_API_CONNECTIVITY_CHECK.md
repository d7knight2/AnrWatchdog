# FlyCI API Connectivity Check Enhancement

## Overview

This enhancement adds comprehensive API connectivity checks to the AnrWatchdog repository to ensure reliable network access to `api.flyci.net` before triggering FlyCI Wingman workflows. The implementation provides early detection of connectivity issues and detailed debugging information to improve the developer experience.

## What Was Implemented

### 1. Reusable Composite Action

Created `.github/actions/flyci-connectivity-check/action.yml` - a reusable GitHub Actions composite action that performs:

- **DNS Resolution Check**: Uses `nslookup` (with `host` fallback) to verify `api.flyci.net` resolves correctly
- **HTTP Connectivity Check**: Uses `curl` to test actual connectivity with HTTPS/HTTP fallback
- **Fail-Fast Behavior**: Exits immediately with error code 1 if any check fails
- **Detailed Logging**: Provides comprehensive output including:
  - DNS server information
  - Resolved IP addresses
  - HTTP response status codes
  - Connection timing and diagnostic information
  - Clear error messages with troubleshooting hints

### 2. Workflow Integration

The connectivity check has been integrated into all workflows that use FlyCI Wingman:

#### Updated Workflows:
- **pr-validation.yml**: 3 jobs (unit-tests, build, ui-tests)
- **android-ci.yml**: 3 jobs (test, build, instrumented-test)
- **appetize-upload.yml**: 1 job (upload-to-appetize)
- **nightly-build.yml**: 1 job (build-and-distribute)

#### Integration Pattern:
```yaml
- name: Check FlyCI API Connectivity
  if: always()
  uses: ./.github/actions/flyci-connectivity-check

- name: FlyCI Wingman
  if: always()
  uses: fly-ci/wingman-action@v1
```

The check is placed immediately before each FlyCI Wingman step and runs with `if: always()` to ensure it executes even if previous steps fail.

## Technical Details

### DNS Resolution Check

The action first attempts to use `nslookup` for DNS resolution:

```bash
nslookup api.flyci.net
```

If `nslookup` is not available, it falls back to the `host` command:

```bash
host api.flyci.net
```

**Output includes:**
- DNS server address
- Resolved IP addresses
- Query success/failure status
- Detailed error messages if resolution fails

### HTTP Connectivity Check

The action performs HTTP connectivity testing using `curl`:

```bash
# Primary HTTPS check
curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 --max-time 15 https://api.flyci.net

# Fallback HTTP check if HTTPS fails
curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 --max-time 15 http://api.flyci.net
```

**Features:**
- 10-second connection timeout
- 15-second maximum time limit
- Accepts any valid HTTP status code (1xx-5xx) as successful connectivity
- Provides verbose output on failure for debugging
- Tests both HTTPS and HTTP protocols

### Error Handling

The action provides clear, actionable error messages:

**DNS Failure Example:**
```
❌ ERROR: DNS resolution failed for api.flyci.net

Possible causes:
  - DNS server is not responding
  - Network connectivity issues
  - Domain does not exist or is temporarily unavailable

Please check:
  1. Network connectivity
  2. DNS server configuration
  3. FlyCI service status
```

**HTTP Failure Example:**
```
❌ ERROR: Unable to connect to api.flyci.net

Connection Details:
  - HTTPS Status: Failed
  - HTTP Status: Failed

Possible causes:
  - Network firewall blocking the connection
  - FlyCI API service is down
  - Network connectivity issues
  - SSL/TLS certificate issues

Please check:
  1. FlyCI service status at https://status.flyci.net
  2. Network connectivity and firewall rules
  3. GitHub Actions runner network configuration
```

## Benefits

### For Developers:
- **Early Failure Detection**: Issues are caught before FlyCI Wingman attempts to run
- **Clear Error Messages**: Detailed, actionable information for troubleshooting
- **Reduced Debugging Time**: Connectivity issues are identified immediately with context
- **Better Visibility**: Comprehensive logs show exactly what's happening

### For Operations:
- **Improved Reliability**: Fail-fast approach prevents wasted CI time on doomed operations
- **Better Monitoring**: Connectivity check provides data points for service health
- **Easier Troubleshooting**: Centralized connectivity checking logic
- **Consistent Behavior**: Same check runs across all workflows

## Testing

### Validation Performed:
1. ✅ YAML syntax validation for all workflow files
2. ✅ YAML syntax validation for composite action
3. ✅ Manual testing of connectivity check logic
4. ✅ Verification of error handling and output formatting

### Test Results:
- All YAML files are valid
- Connectivity check logic executes correctly
- DNS lookup works as expected
- HTTP checks properly handle success and failure scenarios
- Error messages are clear and informative

## Files Modified

### New Files:
- `.github/actions/flyci-connectivity-check/action.yml` - Composite action implementation

### Modified Files:
- `.github/workflows/pr-validation.yml` - Added connectivity check to 3 jobs
- `.github/workflows/android-ci.yml` - Added connectivity check to 3 jobs
- `.github/workflows/appetize-upload.yml` - Added connectivity check to 1 job
- `.github/workflows/nightly-build.yml` - Added connectivity check to 1 job
- `FLYCI_WINGMAN_INTEGRATION.md` - Updated documentation

## Usage

The connectivity check runs automatically as part of the workflow. No manual intervention is required.

### Successful Run Output:
```
========================================
FlyCI API Connectivity Check - DNS Resolution
========================================
Testing DNS resolution for api.flyci.net...

Using nslookup for DNS resolution:
Server:		8.8.8.8
Address:	8.8.8.8#53

Name:	api.flyci.net
Address: 1.2.3.4

✅ DNS resolution successful for api.flyci.net

========================================
FlyCI API Connectivity Check - HTTP Connection
========================================
Testing HTTP connectivity to api.flyci.net...

Using curl for HTTP connectivity test:

HTTPS Response Status: 200

✅ HTTP connectivity successful to api.flyci.net
   Response code: 200

========================================
FlyCI API Connectivity Check - Summary
========================================
✅ All connectivity checks passed successfully!

Results:
  ✅ DNS Resolution: api.flyci.net is resolvable
  ✅ HTTP Connectivity: api.flyci.net is reachable

FlyCI Wingman is ready to use.
========================================
```

## Troubleshooting

### If the connectivity check fails:

1. **Review the workflow logs** for detailed error messages
2. **Check DNS resolution** output to see if the domain resolves
3. **Check HTTP status** codes to understand connection issues
4. **Verify network connectivity** from GitHub Actions runners
5. **Check FlyCI service status** (if a status page is available)
6. **Contact support** if issues persist

### Common Issues:

**Issue**: DNS resolution fails
- **Cause**: Domain doesn't exist or DNS server issues
- **Solution**: Verify domain configuration and DNS server health

**Issue**: HTTP connection fails with timeout
- **Cause**: Network firewall or connectivity issues
- **Solution**: Check network policies and firewall rules

**Issue**: HTTP connection fails with SSL error
- **Cause**: Certificate validation issues
- **Solution**: Verify SSL/TLS certificate is valid and trusted

## Future Enhancements

Potential improvements to consider:

1. **Configurable Timeouts**: Allow customization of connection timeouts via inputs
2. **Retry Logic**: Automatically retry failed checks with exponential backoff
3. **Alternative Endpoints**: Support checking backup/alternative API endpoints
4. **Metrics Collection**: Collect and report connectivity metrics over time
5. **Custom DNS Servers**: Allow specification of custom DNS servers for testing
6. **Caching**: Cache successful checks to reduce redundant testing

## Related Documentation

- [FLYCI_WINGMAN_INTEGRATION.md](FLYCI_WINGMAN_INTEGRATION.md) - Main FlyCI Wingman integration guide
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Composite Actions](https://docs.github.com/en/actions/creating-actions/creating-a-composite-action)

## Conclusion

This enhancement significantly improves the reliability and debuggability of FlyCI Wingman integration by:
- Detecting connectivity issues early in the workflow
- Providing detailed diagnostic information
- Failing fast with clear, actionable error messages
- Reducing troubleshooting time for developers

The implementation is minimal, focused, and follows GitHub Actions best practices for reusable composite actions.
