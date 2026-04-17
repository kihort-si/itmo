#include <linux/module.h>
#include <linux/version.h>
#include <linux/kernel.h>
#include <linux/types.h>
#include <linux/kdev_t.h>
#include <linux/fs.h>
#include <linux/device.h>
#include <linux/cdev.h>
#include <linux/uaccess.h>
#include <linux/slab.h>

static dev_t first;
static struct cdev c_dev; 
static struct class *cl;
static int result_buf[1024];
static int curr_result_pos = 0;

static int my_open(struct inode *i, struct file *f)
{
  printk(KERN_INFO "Driver: open()\n");
  return 0;
}

static int my_close(struct inode *i, struct file *f)
{
  printk(KERN_INFO "Driver: close()\n");
  return 0;
}

static ssize_t my_read(struct file *f, char __user *buf, size_t len, loff_t *off)
{
  printk(KERN_INFO "Driver: read()\n");
  char kern_buf[1024];
  int i = 0;
  int pos = 0;
  printk(KERN_INFO "Whitespace count\n");
  while (i < curr_result_pos) {
    pos += sprintf(kern_buf + pos, "Write %d: %d whitespaces\n", i, result_buf[i]);
    i++;
  }
  int copy = copy_to_user(buf, kern_buf, pos);
  if (copy){
    return -1;
  }
  *off = pos;
  return pos;
}

static ssize_t my_write(struct file *f, const char __user *buf,  size_t len, loff_t *off)
{
  printk(KERN_INFO "Driver: write()\n");
  int i = 0;
  int count = 0;
  char* kern_buf = kmalloc(len * sizeof(char), GFP_KERNEL);
  if(!kern_buf){
        printk(KERN_INFO "Kbuf malloc error");
    return -1;
  }
  int copy = copy_from_user(kern_buf, buf, len);
  if (copy!=0) {
    printk(KERN_INFO "Copy error");
    kfree(kern_buf);
    return -2;
  }
  while (i < len){
    if (kern_buf[i] == ' ') {
      count++;
    }
    i++;
  }
  if(curr_result_pos >= 1024){
    printk(KERN_INFO "Overwriting");
    curr_result_pos = 0;
  }
  result_buf[curr_result_pos] = count;
  curr_result_pos++;
  kfree(kern_buf);
  return len;
}

static struct file_operations mychdev_fops =
{
  .owner = THIS_MODULE,
  .open = my_open,
  .release = my_close,
  .read = my_read,
  .write = my_write
};
 
static int __init ch_drv_init(void)
{
    printk(KERN_INFO "Hello!\n");
    if (alloc_chrdev_region(&first, 0, 1, "ch_dev") < 0)
	  {
		return -1;
	  }
    if ((cl = class_create(THIS_MODULE, "chardrv")) == NULL)
	  {
		unregister_chrdev_region(first, 1);
		return -1;
	  }
    if (device_create(cl, NULL, first, NULL, "mychdev") == NULL)
	  {
		class_destroy(cl);
		unregister_chrdev_region(first, 1);
		return -1;
	  }
    cdev_init(&c_dev, &mychdev_fops);
    if (cdev_add(&c_dev, first, 1) == -1)
	  {
		device_destroy(cl, first);
		class_destroy(cl);
		unregister_chrdev_region(first, 1);
		return -1;
	  } 
    return 0;
}
 
static void __exit ch_drv_exit(void)
{
    cdev_del(&c_dev);
    device_destroy(cl, first);
    class_destroy(cl);
    unregister_chrdev_region(first, 1);
    printk(KERN_INFO "Bye!!!\n");
}
 
module_init(ch_drv_init);
module_exit(ch_drv_exit);
 
MODULE_LICENSE("GPL");
MODULE_AUTHOR("Author");
MODULE_DESCRIPTION("The first kernel module");