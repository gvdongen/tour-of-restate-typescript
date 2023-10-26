# A Tour of Restate

Restate is a system for easily building resilient applications using **distributed durable RPC & async/await**.

This repository contains the code examples for the `Tour of Restate` tutorial, for the Typescript Handler API and Java SDK.
This tutorial takes your through key Restate features by developing an end-to-end ticketing app.

❓ Learn more about Restate from the [Restate documentation](https://docs.restate.dev).

Have a look at the [Tour of Restate tutorial](https://docs.restate.dev/tour) in the documentation to build and run the application in this repository.

## Releasing

To create a new release, push a tag of the form `vX.Y.Z`.
Then [create a release via GitHub](https://github.com/restatedev/tour-of-restate-typescript/releases).

Releases of this repository are referred to by the [documentation](https://github.com/restatedev/documentation).
Please update the version tag referenced on the [Tour of Restate](https://github.com/restatedev/documentation/blob/main/docs/tour.mdx) documentation page.

### Upgrading Typescript SDK
Upgrade the version tag in `typescript/package.json` and rerun the different parts of the tutorial:
```
npm install
npm run build
npm run app
npm run part1
npm run part2
npm run part3
npm run part4
```

An SDK upgrade warrants a new release.

### Upgrading Java SDK
Upgrade the version tag in `java/build.gradle.kts` and rerun the different parts of the tutorial:

```typescript
./gradlew run
./gradlew -PmainClass=dev.restate.tour.part1.AppMain run
./gradlew -PmainClass=dev.restate.tour.part2.AppMain run
./gradlew -PmainClass=dev.restate.tour.part3.AppMain run
./gradlew -PmainClass=dev.restate.tour.part4.AppMain run
```