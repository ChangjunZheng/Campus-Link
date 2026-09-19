package com.campuslink.architecture;

import com.campuslink.CampusLinkApplication;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 架构守护测试（A3-9，CR-028）：把 ADR-012 的四层范式与 N-4 的鉴权纪律从"人工自查"变为**机器强制**。
 *
 * <p>规则来源：技术方案 §2.4（ADR-012 依赖方向）+ CR-026 复评的三条规则输入 ——
 * <b>R-1</b>（裁决：禁止 web 注入 domain 端口 / 调用领域服务）、<b>R-2</b>（边界：web 允许只读引用 domain 类型载体，
 * 如 {@code domain.model} 与 {@code PageResult}）、<b>R-3</b>（domain 只依赖 JDK + lombok + {@code common}）。
 *
 * <p>⚠️ 覆盖边界：本测试只强制**结构约束**，不校验语义（跨上下文调用是否合理、角色模型是否恰当仍靠评审）；
 * 路径级拦截自 CR-031 起由 {@code EndpointAuthorizationManager} 按端点注解执行（G8 守其接线），
 * 但框架**只区分"登录 / 未登录"**——角色与资源级授权（如"仅作者可删"）仍归业务代码。
 * G9（CR-068，处置问题清单 E-011）因此把 {@code /api/v1/admin/**} 的"必须调 {@code requireRole}"补成机器强制，
 * 它守的是**这一层有没有做**，不守**角色取值得当不当**（{@code requireRole(auth, "ROLE_USER")} 仍会通过，
 * 该由端点自己的 403 用例负责——见 AGENTS.md「测试约定」）。
 */
class ArchitectureGuardTest {

    private static final String ROOT_PACKAGE = "com.campuslink";
    private static final String MODULE_PREFIX = "com.campuslink.module.";
    private static final Set<String> LAYERS = Set.of("domain", "application", "infrastructure", "web");

    /** G1：分层依赖方向（ADR-012）——只允许向内依赖 */
    private static final Set<String> FORBIDDEN_DIRECTIONS = Set.of(
            "domain->application", "domain->infrastructure", "domain->web",
            "application->infrastructure", "application->web",
            "infrastructure->web",
            "web->infrastructure");

    /** G2：domain 不得依赖的框架前缀（lombok 与 common 显式放行，R-3） */
    private static final List<String> FORBIDDEN_FRAMEWORK_PREFIXES = List.of(
            "org.springframework.", "com.baomidou.", "org.apache.ibatis.",
            "jakarta.", "io.swagger.", "org.slf4j.");

    private static final String PUBLIC_ENDPOINT = "com.campuslink.common.web.PublicEndpoint";
    private static final String SECURITY_REQUIREMENT = "io.swagger.v3.oas.annotations.security.SecurityRequirement";
    private static final String CURRENT_USER = "com.campuslink.common.web.CurrentUser";
    private static final String BASE_MAPPER = "com.baomidou.mybatisplus.core.mapper.BaseMapper";
    private static final String SECURITY_CONFIG = "com.campuslink.config.SecurityConfig";
    private static final String ENDPOINT_AUTHORIZATION_MANAGER = "com.campuslink.security.EndpointAuthorizationManager";
    private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";

    /** G9（E-011）：管理端 URL 命名空间——框架层只判"登录 / 未登录"，落在这个前缀下的端点必须自己判角色 */
    private static final String ADMIN_PATH_PREFIX = "/api/v1/admin";
    private static final String REQUIRE_ROLE_METHOD = "requireRole";

    private static final Set<String> MAPPING_ANNOTATIONS = Set.of(
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping",
            "org.springframework.web.bind.annotation.RequestMapping");

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT_PACKAGE);
    }

    @Test
    @DisplayName("守护自检：类导入非空、四层与两个上下文都有样本（防规则空转）")
    void guardHasSubjectsToCheck() {
        assertThat(classes).as("未导入任何类，全部规则都会空转").isNotEmpty();

        List<String> layers = classes.stream().map(c -> layerOf(c.getPackageName()))
                .filter(Objects::nonNull).distinct().sorted().toList();
        assertThat(layers).as("四层都要有样本，否则对应规则形同虚设")
                .containsExactly("application", "domain", "infrastructure", "web");

        List<String> contexts = classes.stream().map(c -> contextOf(c.getPackageName()))
                .filter(Objects::nonNull).distinct().sorted().toList();
        assertThat(contexts).contains("account", "forum");

        assertThat(mappingMethods()).as("必须能扫到端点，否则 N-4 规则空转").isNotEmpty();
    }

    @Test
    @DisplayName("G1 分层依赖方向：domain / application / infrastructure / web 只允许向内依赖（ADR-012）")
    void layerDependencyDirections() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaClass origin : classes) {
            String from = layerOf(origin.getPackageName());
            if (from == null) {
                continue;
            }
            for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String to = layerOf(target.getPackageName());
                if (to != null && FORBIDDEN_DIRECTIONS.contains(from + "->" + to)) {
                    violations.add("%s [%s] → %s [%s]".formatted(origin.getName(), from, target.getName(), to));
                }
            }
        }
        assertNoViolations("G1 分层依赖方向", violations);
    }

    @Test
    @DisplayName("G2 domain 不依赖框架：禁 Spring / MyBatis / jakarta / swagger / slf4j（lombok 与 common 放行，R-3）")
    void domainMustNotDependOnFrameworks() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaClass origin : classes) {
            if (!"domain".equals(layerOf(origin.getPackageName()))) {
                continue;
            }
            for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                String targetName = dependency.getTargetClass().getName();
                if (FORBIDDEN_FRAMEWORK_PREFIXES.stream().anyMatch(targetName::startsWith)) {
                    violations.add("%s → %s".formatted(origin.getName(), targetName));
                }
            }
        }
        assertNoViolations("G2 domain 不依赖框架", violations);
    }

    @Test
    @DisplayName("G3 web 与 domain 的边界（R-1 裁决 / R-2 边界）：禁端口注入与领域服务调用，允许只读类型引用")
    void webMustNotDependOnDomainPortsOrServices() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaClass origin : classes) {
            if (!"web".equals(layerOf(origin.getPackageName()))) {
                continue;
            }
            for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String targetPackage = target.getPackageName();
                boolean port = targetPackage.contains(".domain.gateway") && target.isInterface();
                boolean service = targetPackage.contains(".domain.service");
                if (port || service) {
                    violations.add("%s → %s（%s）".formatted(origin.getName(), target.getName(),
                            port ? "domain 端口接口，须经 application" : "领域服务，须经 application"));
                }
            }
        }
        assertNoViolations("G3 web 与 domain 的边界", violations);
    }

    @Test
    @DisplayName("G4 跨上下文：module.<X> 只允许依赖 module.<Y> 的 application 包")
    void crossContextDependenciesMustTargetApplicationLayer() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaClass origin : classes) {
            String fromContext = contextOf(origin.getPackageName());
            if (fromContext == null) {
                continue;
            }
            for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String toContext = contextOf(target.getPackageName());
                if (toContext == null || toContext.equals(fromContext)) {
                    continue;
                }
                if (!"application".equals(layerOf(target.getPackageName()))) {
                    violations.add("%s [%s] → %s [%s]".formatted(origin.getName(), fromContext, target.getName(), toContext));
                }
            }
        }
        assertNoViolations("G4 跨上下文依赖", violations);
    }

    @Test
    @DisplayName("G5 端口实现位置：实现 domain.gateway 接口的类必须是同上下文的 infrastructure")
    void portImplementationsMustLiveInInfrastructure() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaClass origin : classes) {
            for (JavaClass port : origin.getAllRawInterfaces()) {
                if (!port.getPackageName().contains(".domain.gateway")) {
                    continue;
                }
                String implContext = contextOf(origin.getPackageName());
                if (implContext == null || !implContext.equals(contextOf(port.getPackageName()))
                        || !"infrastructure".equals(layerOf(origin.getPackageName()))) {
                    violations.add("%s 实现端口 %s（要求同上下文 infrastructure）".formatted(origin.getName(), port.getName()));
                }
            }
        }
        assertNoViolations("G5 端口实现位置", violations);
    }

    @Test
    @DisplayName("G6 Mapper 位置：BaseMapper 的实现类必须位于 ..mapper.. 包（F-3 的机器防线）")
    void mappersMustLiveInMapperPackages() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaClass origin : classes) {
            boolean isMapper = origin.getAllRawInterfaces().stream()
                    .anyMatch(i -> BASE_MAPPER.equals(i.getName()));
            if (!isMapper) {
                continue;
            }
            String pkg = origin.getPackageName();
            if (!(pkg.endsWith(".mapper") || pkg.contains(".mapper."))) {
                violations.add("%s（%s）".formatted(origin.getName(), pkg));
            }
        }
        assertNoViolations("G6 Mapper 位置", violations);
    }

    @Test
    @DisplayName("G6 @MapperScan 扫描值必须恰为单一通配 com.campuslink.**.mapper（出现特例即失败）")
    void mapperScanMustStaySingleWildcard() {
        MapperScan scan = CampusLinkApplication.class.getAnnotation(MapperScan.class);
        assertThat(scan).as("CampusLinkApplication 必须声明 @MapperScan").isNotNull();
        assertThat(scan.value())
                .as("Mapper 一律放 ..mapper.. 包；扫描列表出现特例（第二条）即失败")
                .containsExactly("com.campuslink.**.mapper");
        assertThat(scan.basePackages()).as("不得改用 basePackages 绕过断言").isEmpty();
        assertThat(scan.basePackageClasses()).as("不得改用 basePackageClasses 绕过断言").isEmpty();
    }

    @Test
    @DisplayName("G7 N-4：web 映射方法必须显式二选一——@PublicEndpoint（公开）或 @SecurityRequirement（受保护）")
    void webMappingMethodsMustDeclarePublicOrProtected() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaMethod method : mappingMethods()) {
            boolean markedPublic = hasAnyAnnotation(method, Set.of(PUBLIC_ENDPOINT));
            boolean markedProtected = hasAnyAnnotation(method, Set.of(SECURITY_REQUIREMENT));
            if (markedPublic == markedProtected) {
                violations.add("%s（%s）".formatted(describe(method),
                        markedPublic ? "两者同时声明，必须二选一" : "两者都未声明——漏写鉴权会静默变成公开接口"));
            }
        }
        assertNoViolations("G7 端点鉴权声明", violations);
    }

    @Test
    @DisplayName("G7 N-4：声明 @SecurityRequirement 的方法必须真的调用 CurrentUser 鉴权入口（契约 ≠ 运行时强制）")
    void protectedEndpointsMustCallCurrentUser() {
        Set<String> violations = new LinkedHashSet<>();
        for (JavaMethod method : mappingMethods()) {
            if (!hasAnyAnnotation(method, Set.of(SECURITY_REQUIREMENT))) {
                continue;
            }
            boolean callsCurrentUser = method.getMethodCallsFromSelf().stream()
                    .anyMatch(call -> CURRENT_USER.equals(call.getTargetOwner().getName()));
            if (!callsCurrentUser) {
                violations.add("%s：声明了 @SecurityRequirement 却未调用 CurrentUser".formatted(describe(method)));
            }
        }
        assertNoViolations("G7 受保护端点的运行时鉴权", violations);
    }

    @Test
    @DisplayName("G8 CR-031：SecurityConfig 必须依赖 EndpointAuthorizationManager，且不得再调 permitAll（路径级鉴权不被摘除）")
    void securityConfigMustEnforcePathAuthorization() {
        Set<String> violations = new LinkedHashSet<>();
        JavaClass securityConfig = classes.stream()
                .filter(candidate -> SECURITY_CONFIG.equals(candidate.getName()))
                .findFirst().orElse(null);
        if (securityConfig == null) {
            violations.add("未导入 %s——本规则失去对象".formatted(SECURITY_CONFIG));
        } else {
            boolean wired = securityConfig.getDirectDependenciesFromSelf().stream()
                    .anyMatch(dependency -> ENDPOINT_AUTHORIZATION_MANAGER.equals(dependency.getTargetClass().getName()));
            if (!wired) {
                violations.add("SecurityConfig 未依赖 EndpointAuthorizationManager——路径级鉴权被摘除后全部端点一律放行，"
                        + "@PublicEndpoint / @SecurityRequirement 的声明将失去运行时意义");
            }
            for (JavaMethod method : securityConfig.getMethods()) {
                for (JavaMethodCall call : method.getMethodCallsFromSelf()) {
                    if ("permitAll".equals(call.getTarget().getName())
                            && call.getTargetOwner().getPackageName().startsWith("org.springframework.security")) {
                        violations.add("%s 调用了 permitAll()：全通放行等于取消鉴权".formatted(describe(method)));
                    }
                }
            }
        }
        assertNoViolations("G8 路径级鉴权", violations);
    }

    @Test
    @DisplayName("G9 E-011：/api/v1/admin/** 的映射方法必须真的调用 CurrentUser.requireRole（角色线不得被静默摘除）")
    void adminEndpointsMustEnforceRole() {
        Set<String> adminEndpoints = new LinkedHashSet<>();
        Set<String> violations = new LinkedHashSet<>();
        for (JavaClass origin : classes) {
            if (!"web".equals(layerOf(origin.getPackageName()))) {
                continue;
            }
            List<String> classPaths = declaredPaths(origin.getAnnotations(), Set.of(REQUEST_MAPPING));
            for (JavaMethod method : origin.getMethods()) {
                if (!hasAnyAnnotation(method, MAPPING_ANNOTATIONS)) {
                    continue;
                }
                boolean isAdmin = isAdminPath(classPaths, declaredPaths(method.getAnnotations(), MAPPING_ANNOTATIONS));
                if (!isAdmin) {
                    continue;
                }
                adminEndpoints.add(describe(method));
                boolean callsRequireRole = method.getMethodCallsFromSelf().stream()
                        .anyMatch(call -> CURRENT_USER.equals(call.getTargetOwner().getName())
                                && REQUIRE_ROLE_METHOD.equals(call.getTarget().getName()));
                if (!callsRequireRole) {
                    violations.add("%s：位于 %s 之下却未调用 CurrentUser.requireRole——EndpointAuthorizationManager 只区分"
                                    + "登录 / 未登录，任何已登录账号（含普通用户）都能调用它。摘掉 requireRole 曾能通过全部守护测试"
                                    + "（问题清单 E-011 的反证实验），本规则即为其机器防线".formatted(describe(method), ADMIN_PATH_PREFIX + "/**"));
                }
            }
        }
        if (adminEndpoints.isEmpty()) {
            violations.add("未扫到任何 %s 端点——本规则失去对象。换 URL 前缀、或把管理端点搬出 module/*/web，"
                    .formatted(ADMIN_PATH_PREFIX + "/**") + "都会让角色线重新无人看守，须连同本规则一起改");
        }
        System.out.printf("[G9] 受角色强制的管理端点共 %d 个：%s%n", adminEndpoints.size(), adminEndpoints);
        assertNoViolations("G9 管理端点的角色强制", violations);
    }

    /** 类级 + 方法级路径的笛卡尔组合里，是否存在落在 {@link #ADMIN_PATH_PREFIX} 命名空间下的有效路径 */
    private static boolean isAdminPath(List<String> classPaths, List<String> methodPaths) {
        for (String classPath : classPaths.isEmpty() ? List.of("") : classPaths) {
            for (String methodPath : methodPaths.isEmpty() ? List.of("") : methodPaths) {
                String head = classPath.endsWith("/") ? classPath.substring(0, classPath.length() - 1) : classPath;
                String tail = methodPath.startsWith("/") ? methodPath : "/" + methodPath;
                String effective = (head + tail).replaceAll("/+", "/");
                if (effective.equals(ADMIN_PATH_PREFIX) || effective.startsWith(ADMIN_PATH_PREFIX + "/")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 映射注解上显式声明的路径（Spring 的 {@code path} 与 {@code value} 是同义别名，取显式声明那一个即可）。
     *
     * <p>⚠️ 只读<b>显式声明</b>的属性：默认值是空数组，读它会把形态判断推给 ArchUnit 的内部表示。
     * 实测 javac 把 {@code @PutMapping("/x")} 写成 {@code String[]{"/x"}}（数组而非集合），故数组与集合两种形态都要认；
     * 遇到都认不出的形态时**抛异常让规则变红**，而不是静默放行——静默放行正是 E-011 的病灶。
     */
    private static List<String> declaredPaths(Collection<? extends JavaAnnotation<?>> annotations, Set<String> typeNames) {
        List<String> paths = new ArrayList<>();
        for (JavaAnnotation<?> annotation : annotations) {
            if (!typeNames.contains(annotation.getRawType().getName())) {
                continue;
            }
            for (String property : List.of("path", "value")) {
                Object declared = annotation.tryGetExplicitlyDeclaredProperty(property).orElse(null);
                if (declared == null) {
                    continue;
                }
                List<Object> elements = switch (declared) {
                    case String single -> List.of(single);
                    case Object[] group -> List.of(group);
                    case Collection<?> group -> List.copyOf(group);
                    default -> throw new IllegalStateException("G9 无法解析路径声明形态：" + declared);
                };
                for (Object element : elements) {
                    if (!(element instanceof String path)) {
                        throw new IllegalStateException("G9 无法解析路径声明形态：" + element);
                    }
                    if (!path.isBlank()) {
                        paths.add(path); // 显式写了空串等价于不写路径
                    }
                }
            }
        }
        return paths;
    }

    /** module 四层内的全部 HTTP 映射方法（含 web 子包） */
    private static List<JavaMethod> mappingMethods() {
        List<JavaMethod> methods = new ArrayList<>();
        for (JavaClass origin : classes) {
            if (!"web".equals(layerOf(origin.getPackageName()))) {
                continue;
            }
            for (JavaMethod method : origin.getMethods()) {
                if (hasAnyAnnotation(method, MAPPING_ANNOTATIONS)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    private static boolean hasAnyAnnotation(JavaCodeUnit codeUnit, Set<String> annotationTypeNames) {
        return codeUnit.getAnnotations().stream()
                .map(annotation -> annotation.getRawType().getName())
                .anyMatch(annotationTypeNames::contains);
    }

    /** com.campuslink.module.<context>.<layer>[.…] → layer；不是四层结构返回 null */
    private static String layerOf(String packageName) {
        return partAfterModulePrefix(packageName, 1);
    }

    /** com.campuslink.module.<context>[.…] → context；不在 module 包下返回 null */
    private static String contextOf(String packageName) {
        return partAfterModulePrefix(packageName, 0);
    }

    private static String partAfterModulePrefix(String packageName, int index) {
        if (!packageName.startsWith(MODULE_PREFIX)) {
            return null;
        }
        String[] parts = packageName.substring(MODULE_PREFIX.length()).split("\\.");
        if (index == 0) {
            return parts[0].isEmpty() ? null : parts[0];
        }
        return parts.length > index && LAYERS.contains(parts[index]) ? parts[index] : null;
    }

    private static String describe(JavaMethod method) {
        return "%s#%s".formatted(method.getOwner().getName(), method.getName());
    }

    private static void assertNoViolations(String rule, Collection<String> violations) {
        String detail = "%s发现 %d 处违规：\n  - %s".formatted(rule, violations.size(), String.join("\n  - ", violations));
        assertThat(violations).as(detail).isEmpty();
    }
}
