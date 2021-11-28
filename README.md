## Xác thực request bằng Spring Boot Validation

Xác thực dữ liệu đầu vào (data validation) là một trong những yêu cầu cơ bản cho hầu hết các ứng dụng, đặc biệt là các ứng dụng web. Để hỗ trợ người dùng thì Spring Framework đặt ra các [chuẩn xác thực dữ liệu](https://beanvalidation.org/1.0/spec/) và cung cấp các công cụ để hỗ trợ lập trình viên trong tác vụ "tưởng như cơ bản nhưng chủ quan thì sẽ phải trả giá" này.

### 1. Khai báo dependency
Chúng ta sẽ sử dụng thư viện ***spring-boot-starter-validation*** để thực hiện xác thực. Đây là cách khai báo sử dụng thư viện trong Maven:

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

*Note: Một số nguồn nói rằng spring-boot-starter-validation đã có sẵn trong dependency spring-boot-starter-web. Tuy nhiên điều này không còn đúng từ Spring Boot 2.3 và trở đi: lập trình viên sẽ phải tự khai báo thư viện validation của Spring như trên.*

### 2. Mô tả ví dụ
Chúng ta sẽ thử xác thực một request tạo người (PersonRequest). Trong request để tạo Person sẽ có các thuộc tính sau: tên (name), tuổi (age), chiều cao (height), danh sách các sở thích (hobbies):

```java
public class PersonRequest {
    private String name;
    private Long age;
    private Double height;
    private List<String> hobbies;
}
```

Controller của chúng ta sẽ có dạng như sau:

```java
@RestController(value = "RestController")
@RequestMapping(path = "/person")
public class PersonController {
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createPerson(@RequestBody @Valid PersonRequest request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "Một hoặc nhiều trường truyền vào không hợp lệ!";
        }
        //TODO: Thêm code gọi xuống service layer
        return "Các trường truyền vào hợp lệ!";
    }
}

```

@Valid và BindingResult sẽ được giải thích ở bên dưới.

### 3. Các annotation thông dụng trong việc xác thực dữ liệu.

Spring Validation cung cấp cho chúng ta hàng loạt các annotation (chú thích) trong package **javax.validation.constraints** để hỗ trợ việc xác thực dữ liệu. Hãy cùng nhau điểm qua một vài gương mặt tiêu biểu:

#### @NotNull, @NotEmpty và @NotBlank
Như tên gọi của nó, annotation **@NotNull** kiểm tra xem liệu thuộc tính có null hay không.

Khi chúng ta thêm @NotNull vào thuộc tính tên (name) như sau:

```java
@NotNull
private String name;
```

Nếu như trong request truyền vào không có trường name, hoặc giá trị truyền vào là null thì kết quả khi gọi vào endpoint để tạo Person bên trên thì kết quả trả về sẽ là lỗi (minh ở đây sử dụng Postman):
![image](https://user-images.githubusercontent.com/94212764/143775360-dfca38c1-9ecc-4070-b5cf-db108e64e8d8.png)

Tuy nhiên nếu như tên được truyền vào là một String rỗng ("") thì kết quả lại như sau:
![image](https://user-images.githubusercontent.com/94212764/143775512-48eb188c-7ee3-4297-8d27-4a5ca0236579.png)

Vì vậy, ngoài @NotNull, chúng ta còn có **@NotEmpty và @NotBlank**. Khi so sánh chúng thì mình kết luận như sau:

@NotNull: có thể *dùng được cho **hầu như tất cả các kiểu dữ liệu** trong Java*, **từ chối giá trị null** hoặc không có **nhưng chấp nhận giá trị rỗng**. *Note: Thậm chí annotation này có thể dùng được cho các kiểu dữ liệu nguyên thủy (int, long, float, double, char, byte...) mà không báo lỗi (mặc dù các kiểu nguyên thủy không thể null) nhưng mà nó sẽ không có tác dụng gì cả.*

@NotEmpty: có thể dùng được cho CharSequence *(interface của String, StringBuffer và StringBuilder)*, Collection, Map, Array. Annotation này từ chối **cả giá trị null lẫn giá trị rỗng bằng cách kiểm tra độ dài (length) (String) hoặc kích thước (size) (Collection) xem có lớn hơn 0 hay không**.

@NotBlank: có thể dùng được cho String. Annotation này từ chối String có giá trị null và **String có độ dài là 0 sau khi đã trim** *(loại bỏ hết khoảng trắng thừa ở đầu và cuối của String)*

#### @Min, @Max
Hai annotation trên được sử dụng để kiểm trị số được truyền vào có lớn hơn mức tối thiểu(**@Min**) và có lớn hơn mức tối đa (**@Max**) không.

Khi chúng ta chú thích cho tuổi (age) như sau:

```java
@Min(0)
@Max(200)
private Long age;
```

Thì tất cả các giá trị truyền vào mà nằm trong khoảng từ 0 đến 200 *(bao gồm 0 và 200)* sẽ được coi là hợp lệ và ngược lại.

Fun fact 1: *@Min(0) là viết tắt của @Min(value= 0)*

Fun fact 2: **@Min, @Max ngoài sử dụng cho int, long, short, byte còn có thể sử dụng được cho String!** (miễn là String có thể được parse thành số, nếu không validate sẽ fail). **Hơn nữa, khi sử dụng cho String thì @Min @Max sẽ dùng được cho cả số thập phân, hoặc bạn có thể xem @DecimalMin/ @DecimalMax phần tiếp theo:**

#### @Digits, @DecimalMin, @DecimalMax

Ba annotation này được thiết kế *để sử dụng với số thập phân (Double, FLoat, BigDecimal)*. 

**@Digits** kiểm tra định dạng (format) của số thập phân. Trong @Digits có hai giá trị có thể truyền vào, đó là **integer và fraction**. Giá trị integer sẽ giới hạn độ dài của phần nguyên (phần bên trái dấu thập phân) và giá trị fraction sẽ giới hạn độ dài của phần thập phân (phần bên phải dấu thập phân).

Ví dụ, nếu như ta chú thích cho chiều cao (height) như sau: 

```java
@Digits(integer = 3, fraction = 2)
private Double height;
```

Thì Spring sẽ kiểm tra xem giá trị của thuộc tính liệu có thuộc dạng ###.## hay không. Trong trường hợp này, các giá trị hợp lệ là: 100; 70.5; 150.52. Còn 1545.1 hoặc 170.351 là các giá trị **không hợp lệ (lưu ý rằng phần thập phân nếu có nhiều số hơn so với quy ước thì Spring sẽ không làm tròn mà trả lại lỗi!)**.

**@DecimalMin và @DecimalMax** cũng có cùng mục đích với @Min và @Max là dùng để giới hạn số trong một khoảng nhất định. Điểm khác biệt là @DecimalMin và @DecimalMax có một tham số bắt buộc là **value** để truyền giá trị so sánh *(Note: kiểu String)*, ngoài ra còn có một tham số không bắt buộc nữa nữa là **inclusive** để cho Spring biết liệu chính giá trị được truyền trong value có được chấp nhận hay không *(giá trị mặc định là **true**)*. Bây giờ chúng ta sẽ cập nhật chiều cao (height) như sau:

```java
@DecimalMin(value = "0.0", inclusive = false)
@DecimalMax(value = "300.5", inclusive = true)
@Digits(integer = 3, fraction = 2)
private Double height;
```
Thì 300.5 sẽ là một giá trị hợp lệ vì inclusive dược đặt là true, tuy nhiên **0 sẽ là một giá trị không hợp lệ** vì inclusive là false.

#### @Pattern
Annotation này được sử dụng để so sánh liệu CharSequence có khớp với một regular expression (regexp). Lưu ý @Pattern sẽ chỉ coi CharSequence là hợp lệ khi **toàn bộ CharSequence khớp với regular expression. Nếu chỉ một ký tự không match với regular expression thì sẽ bị coi là không hợp lệ**. Nếu không chắc, hãy sử dụng [regex101](https://regex101.com/) và chọn Flavor là Java 8 để kiểm tra regexp của bạn với bất kì dữ liệu đầu vào.

Khi chúng ta chú thích cho tên (name) như sau:

```java
@Pattern(regexp = "[a-zA-Z][a-zA-Z ]+")
private String name;
```
Thì tất cả các tên được truyền vào buộc phải có ký tự đầu tiên là chữ cái ASCII, và các kí tự sau đó phải là kí chữ cái hoặc dấu cách. Nếu như String chứa số hoặc các kí tự đặc biệt (% & * # @ ! ...) thì sẽ không xác thực thành công. *Note: regexp trên sẽ không dùng được cho chữ cái của các ngôn ngữ khác, trong đó có tiếng Việt.*

#### @Length
Để ép độ dài cho String thì ta có thể dùng annotation **@Length** thuộc package **org.hibernate.validator.constraints** và chỉ định giá trị min và/hoặc max. (vẫn tương thích với ví dụ bên trên):

```java
@Length(min = 3, max = 200)
private String name;
```

#### @Size
Tương tự, để ép kích thước cho một tập hợp thì có thể dùng **@Size**:

```java
@Size(min = 2, max = 100)
private List<String> hobbies;
```

#### @Valid
Trong controller, chúng ta chú thích **@Valid cho đối số cần được xác thực dữ liệu**, ví dụ:

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public String createPerson(@RequestBody @Valid PersonRequest request)
```

### 4. Xử lý lỗi xác thực bằng BindingResult

BindingResult là nơi Spring chứa kết quả của việc xác thực dữ liệu. Với ví dụ của chúng ta, nếu như có lỗi trong quá trình xác thực đầu vào thì chúng ta có thể kiểm tra bằng **BindingResult.hasErrors()**:

```java
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createPerson(@RequestBody @Valid PersonRequest request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "Một hoặc nhiều trường truyền vào không hợp lệ!";
        }
	...
```

Tuy bắt được lỗi xác thực, xong cách trên tương đối vụng về: *Người dùng sẽ không biết được cụ thể (những) trường nào có lỗi và vì sao lại bị lỗi*. Để xử lý tinh tế hơn thì chúng ta phải sử dụng **BindingResult.getFieldErrors** để lấy về tất cả các trường bị lỗi xác thực, bao gồm tên trường và tin nhắn trả ra khi bị lỗi như sau:

```java
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createPerson(@RequestBody @Valid PersonRequest request, BindingResult bindingResult) {

        if(bindingResult.hasErrors()){
            Map<String, String> errors= new HashMap<>();

            bindingResult.getFieldErrors().forEach(
                    error -> errors.put(error.getField(), error.getDefaultMessage())
            );

            String errorMsg= "";

            for(String key: errors.keySet()){
                errorMsg+= "Lỗi ở: " + key + ", lí do: " + errors.get(key) + "\n";
            }
            return errorMsg;
        }

        //TODO: Thêm code gọi xuống service layer
        log.info(request.toString());
        return "Các trường truyền vào hợp lệ!";
    }
```

Sau đó chúng ta thử gọi đến API với 4 trường bị lỗi:

![image](https://user-images.githubusercontent.com/94212764/143779530-1133db3a-c0dc-4b56-860c-d2933d7fd787.png)

Để tinh chỉnh tin nhắn lỗi trả ra theo ý muốn, ví dụ như trả ra tiếng Việt thay vì tiếng anh, chúng ta có thể bổ sung giá trị **message** cho các annotation thuộc kiểu constraints (được chú thích bởi @Constraint) như sau:

```java
public class PersonRequest {
    @NotNull(message = "Tên bị null!")
    @Pattern(regexp = "[a-zA-Z][a-zA-Z ]+", message = "Tên chứa kí tự cấm!")
    @Length(min = 3, max = 200, message = "Tên phải từ 3 đến 200 kí tự!")
    private String name;

    @Min(value = 0, message = "Tuổi không được bé hơn 0!")
    @Max(value = 200, message = "Tuổi không được lớn hơn 200!")
    private Long age;

    @DecimalMin(value = "0.0", inclusive = false, message = "Chiều cao phải là số dương!")
    @DecimalMax(value = "300.5", inclusive = true, message = "Chiều cao không được cao quá 300.5 cm!")
    @Digits(integer = 3, fraction = 2
            , message = "Chiều cao không khớp định dạng tối đa 3 số phần nguyên và 2 số phần thập phân!")
    private Double height;

    @Size(min = 2, max = 100, message = "Phải có ít nhất 2 sở thích!")
    private List<String> hobbies;
}
```

Thì kết quả báo lỗi sẽ thay đổi tương ứng: 

![image](https://user-images.githubusercontent.com/94212764/143779834-223c4326-90ef-45c4-ab64-62ad47764506.png)


### 5. Custom Validator

Để tự định nghĩa một quy trình xác thực dữ liệu riêng, đầu tiên ta cần phải tạo một annotation dạng Constraint. Ví dụ ở đây mình sẽ làm một constraint (ràng buộc) là chữ cái đầu tiên của một String phải được in hoa:

```java
@Documented
@Constraint(validatedBy = CapitalizedValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CapitalizedConstraint {
    String message() default "Chữ đầu tiên phải được in hoa!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

Sau khi tạo Constraint xong thì ta phải tạo Validator tương ứng để định nghĩa logic xác thực dữ liệu:

```java
public class CapitalizedValidator implements ConstraintValidator<CapitalizedConstraint, String> {
    @Override
    public void initialize(CapitalizedConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(!StringUtils.hasLength(value)) return Boolean.FALSE;
        if(!Character.isUpperCase(value.charAt(0))) return Boolean.FALSE;

        return Boolean.TRUE;
    }
}
```

Cuối cùng, chúng ta thêm annotation cho thuộc tính mong muốn:

```java
@CapitalizedConstraint
private String name;
```

![image](https://user-images.githubusercontent.com/94212764/143780434-99c52c93-6539-496e-9e02-ef10adf66397.png)


### 6. Xác thực ở Entity?

Thực tế, các annotation được giới thiệu ở mục 3 có thể dùng cho Entity class (phục vụ cho việc lưu vào database *(persistence)*). Tuy nhiên, theo ý kiến cá nhân của mình thì nên validate dữ liệu ngay từ request vì đến Entity mới validate đồng nghĩa vời việc các hàm ở service layer có thể đã phải làm việc với một object méo mó, sai định dạng mà chúng ta mong đợi. Còn các bạn nhận định sao về vấn đề này? Hãy comment bên dưới để chia sẻ với mọi người!

### 7. Tham khảo thêm

[Code các bạn có thể xem tại đây](https://github.com/NguyenDuyThaiSon-OneMount/learnvalidation)

Bài viết tham khảo các nguồn sau đây:

[Bean Validation with Spring Boot](https://reflectoring.io/bean-validation-with-spring-boot/)

[Bean Validation in Spring Boot](https://springframework.guru/bean-validation-in-spring-boot/)

[Java Bean Validation Not Null, Empty, Blank](https://www.baeldung.com/java-bean-validation-not-null-empty-blank)

[Spring Boot Bean Validation](https://www.baeldung.com/spring-boot-bean-validation)

[BindingResult API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/validation/BindingResult.html)

[Spring MVC Custom Validator](https://www.baeldung.com/spring-mvc-custom-validator)

