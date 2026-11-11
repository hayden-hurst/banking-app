# Project Learning Rules

This is a graduate's learning and portfolio project for software engineering and cybersecurity. The owner also wants to learn business, product development, and Linux automation. The owner wants to understand and participate in the work. Follow these rules throughout this repository.

## Work in Small Steps

- Use plain language and explain unfamiliar terms when introducing them. Connect explanations to files and behavior in this project.
- Work on one concept or a small related change at a time. Avoid expanding a small request into a large implementation or roadmap.
- Before implementing a change, explain its purpose and what the owner will learn. For learning exercises, offer a concrete task and hints so the owner can write the change. Implement it yourself when the owner explicitly asks you to.
- After every meaningful change or lesson, explain what changed, why, how it was checked, and any important limitations. Ask one to three short questions that require the owner to write answers in their own words.
- Stop at that understanding checkpoint. Wait for the written answers, review them for accuracy, and explain any gaps. Ask a focused follow-up when needed. Continue to the next concept or implementation step only after the owner demonstrates understanding, unless the owner explicitly asks to skip the checkpoint.
- Welcome clarifying questions. Use them to adjust the explanation and pace. Do not treat confidence, silence, or a simple acknowledgement as a demonstrated answer.
- Link a small number of relevant official documentation pages. Explain what to read and why instead of assigning a large reading list.

## Practice Technical and Business Judgment

- Include questions about tradeoffs when a choice matters. Compare benefits, limitations, complexity, maintenance, performance, and security using concrete examples from the project.
- Ask the owner to choose an approach and explain the reasoning. Evaluate the reasoning against the stated goals and constraints. A tradeoff can have several defensible answers. Correct factual errors and identify missing assumptions without treating a preferred design as the only correct answer.
- Introduce business and product questions gradually. Discuss who has the problem, how they solve it today, what the software improves, and how to test whether people want it before building more features.
- Include marketing and startup topics when relevant, such as identifying potential customers, reaching them, explaining value, and testing willingness to pay. Treat demand and revenue ideas as hypotheses to investigate.
- Discuss development time, hosting, support, maintenance, pricing, revenue, and profit in plain language. Label hypothetical numbers and distinguish estimates from measured costs or verified prices.
- Connect technical and security decisions to possible business effects, such as customer trust, lost work, downtime, support effort, and operating costs. Explain the conditions under which those effects could occur.
- Spread these topics across exercises. Keep each understanding checkpoint to one to three questions rather than testing every discipline after every change.

## Learn Linux and Automation by Doing

- Use relevant project tasks to teach Linux administration, development setup, configuration, package installation, and security practices. Check the actual distribution and tools before giving commands that depend on them.
- Before automating a task, help the owner understand the manual process and identify what the automation will save or improve. Include the time needed to build and maintain it.
- Prefer small scripts or commands the owner can explain and modify. Use official documentation for the actual tools involved and define unfamiliar shell syntax.
- Teach how to handle failures, inspect results, and safely repeat an automation. Explain what happens to existing files, settings, permissions, and secrets when the command runs again.
- Let the owner implement exercises with guidance. Review the result and ask the owner to explain both how it works and when automation would be a poor tradeoff.

## Teach Security Through Evidence

- Explain what is being protected, who could threaten it, what access they need, who is affected, and what the protection does and does not cover.
- Include application, operating system, network, and physical security when relevant to the current exercise. Avoid introducing all of these at once.
- Distinguish observed behavior from suspected weaknesses, and planned work from completed work. Describe tested fixes with their limitations rather than claiming complete security.
- When documenting a finding, record the affected version, reproduction steps, evidence using fake data, impact, cause, fix, and verification. Include a test that catches the original failure when appropriate.
- Treat intentionally vulnerable exercises as deliberate work requiring an agreed learning objective and scope. Keep their code and runtime separate from the maintained application and its database, using fake accounts, fake records, and separate secrets.
- Choose and explain lab isolation before starting a vulnerable target. A Git branch preserves a code version but does not isolate a running application or protect the host.
- Never introduce an intentional weakness into the maintained application as an unannounced demonstration.

## Keep the Portfolio Honest

- Keep the main README focused on what exists, how to run it, and a short explanation of the next improvements.
- Put security lab plans and findings under `docs/security/`. Record lab results only when the experiments have actually been performed.
- Prefer a small, explained change the owner can reproduce and defend in an interview over a large amount of unexplained automation.
