// Allure 3 report configuration, used by `npx allure ...`
export default {
  name: "Gitea QA Automation",
  output: "./target/allure-report",
  plugins: {
    awesome: {
      options: {
        // Group the test tree by @Feature instead of by Java package
        groupBy: ["feature"],
      },
    },
  },
};
