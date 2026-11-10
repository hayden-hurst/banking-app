# Security Learning Lab

This is the plan for a future practice lab built around the Banking App. The aim is to learn how weaknesses happen, who they affect, and how to investigate and fix them. Experiments and results will be added as they are completed.

## How Each Exercise Will Work

Choose one question, such as whether one user can read another user's bank account. First describe the expected protection and the part of the application being tested. Then investigate, record the result, and make a small fix if a weakness is found. A final check should show that the unwanted behavior is blocked and the intended behavior still works.

Before moving on, explain the finding and fix in your own words. The teaching rules apply throughout the project. [Project learning rules](../../AGENTS.md)

## Keep Versions and Running Environments Separate

The maintained version will contain the latest reviewed fixes. A proposed lab branch, such as `lab/account-access`, can preserve an intentionally vulnerable example. Each report should identify the exact Git commits before and after a fix so the experiment can be repeated.

A branch only separates code history. Before running a vulnerable example, choose a separate environment, such as a disposable virtual machine with restricted networking, and verify who can reach it. Use a separate database, fake records, and separate example keys. The current Compose setup publishes ports, so we need to review its network access before using it for vulnerable exercises.

The maintained version can also be tested for previously unknown weaknesses. Record what was actually tested and avoid presenting a passing test as proof that the entire application is secure.

## Questions to Explore Over Time

| Area | Example learning question |
|---|---|
| Application | Can a logged-in user access an account belonging to someone else? |
| System | Which files and processes can the application's operating system user access? |
| Network | Who can connect to the API and database, and is information encrypted while travelling? |
| Physical | What could someone access through an unlocked computer or a stolen backup drive? |

For each question, identify the information or service being protected and the access an attacker would need. This is the beginning of **threat modeling**, which means thinking through what could go wrong and how to reduce the risk. The introduction provides a starting point for the first exercise. [OWASP threat modeling guide](https://cheatsheetseries.owasp.org/cheatsheets/Threat_Modeling_Cheat_Sheet.html)

## Format for a Finding

Create one Markdown file per investigation here when an exercise begins. Fill it with your own observations and evidence.

1. **Question and scope.** What are you testing, and which local environment is involved?
2. **Affected version and target.** Which commit, endpoint, file, setting, or device is involved? Is the weakness intentional or newly discovered?
3. **Required access.** Does someone need an account, network access, administrator rights, or physical access?
4. **Reproduction and evidence.** What steps produced the behavior? Use fake data and remove secrets from captured output.
5. **Impact.** Who is affected, and could information be exposed, changed, or made unavailable?
6. **Cause and patch.** Why did it happen, what change fixes it, and which commit contains the patch?
7. **Verification and prevention.** Repeat the original check after the patch, check intended behavior, and describe how future changes can avoid reintroducing the weakness.
8. **What you learned.** Explain the lesson and any remaining uncertainty in your own words.

## References for Later Exercises

- Use the testing guide's authentication and authorization sections when choosing an application access exercise. Authentication checks identity. Authorization checks what that identity is allowed to do. [OWASP Web Security Testing Guide](https://owasp.org/www-project-web-security-testing-guide/)
- For future physical security exercises, consult the Physical and Environmental Protection controls in NIST SP 800-53. This is a reference to look up specific protections as needed. [NIST security and privacy controls](https://csrc.nist.gov/pubs/sp/800/53/r5/upd1/final)
